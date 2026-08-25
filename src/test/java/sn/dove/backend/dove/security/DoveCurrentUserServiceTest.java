package sn.dove.backend.dove.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import sn.dove.backend.dove.config.DoveProperties;
import sn.dove.backend.dove.service.DoveResourceStore;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.ObjectNode;

class DoveCurrentUserServiceTest {

    private final InMemoryResourceStore store = new InMemoryResourceStore();
    private final DoveCurrentUserService service = new DoveCurrentUserService(store, new DoveProperties());

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void invalidatingAUserMakesAnAdditionalPermissionEffectiveImmediately() {
        ObjectNode author = document("ue-1");
        author.put("externalSubject", "subject-ue-1");
        author.put("role", "USER_ENABLEMENT");
        store.add("utilisateurs", author);
        SecurityContextHolder.getContext().setAuthentication(jwtAuthentication("subject-ue-1"));

        assertThat(service.permissions(service.requireUser())).doesNotContain("VALIDATE_CONTENT");

        ObjectNode validator = author.deepCopy();
        validator.putArray("additionalPermissions").add("VALIDATE_CONTENT");
        store.replace("utilisateurs", validator);

        assertThat(service.permissions(service.requireUser())).doesNotContain("VALIDATE_CONTENT");

        service.invalidateUser(validator);

        assertThat(service.permissions(service.requireUser())).contains("VALIDATE_CONTENT");
    }

    @Test
    void mutationScopeAllowsEnablementContentAcrossJobsOfTheSameBusinessUnit() {
        ObjectNode user = JsonNodeFactory.instance.objectNode();
        user.put("role", "USER_ENABLEMENT");
        ObjectNode scope = user.putObject("accessScope");
        scope.put("global", false);
        scope.putArray("businessUnitIds").add("bu-1");
        scope.putArray("businessJobIds");
        scope.putArray("applicationIds");
        scope.putArray("moduleIds");
        ObjectNode sameUnitJob = document("job-same-unit");
        sameUnitJob.put("businessUnitId", "bu-1");
        ObjectNode otherUnitJob = document("job-other-unit");
        otherUnitJob.put("businessUnitId", "bu-2");
        ObjectNode application = document("app-1");
        application.putArray("businessUnitIds").add("bu-1").add("bu-2");
        application.putArray("businessJobIds").add("job-same-unit").add("job-other-unit");
        ObjectNode module = document("module-1");
        module.put("applicationId", "app-1");
        module.putArray("businessUnitIds").add("bu-1").add("bu-2");
        module.putArray("businessJobIds").add("job-same-unit").add("job-other-unit");
        store.add("metiers", sameUnitJob);
        store.add("metiers", otherUnitJob);
        store.add("applications", application);
        store.add("modules", module);

        ObjectNode content = JsonNodeFactory.instance.objectNode();
        content.put("applicationId", "app-1");
        content.put("moduleId", "module-1");
        content.putArray("businessJobIds").add("job-same-unit");
        content.putArray("businessUnitIds").add("bu-1");

        assertThat(service.isInMutationScope(user, content)).isTrue();

        content.putArray("businessJobIds").add("job-other-unit");
        assertThat(service.isInMutationScope(user, content)).isFalse();
    }

    @Test
    void aNewApplicationAndModuleAreVisibleFromTheUsersJobWithoutUpdatingTheUser() {
        ObjectNode user = document("user-1");
        user.put("role", "NANDITE");
        ObjectNode scope = user.putObject("accessScope");
        scope.put("global", false);
        scope.putArray("businessUnitIds").add("bu-1");
        scope.putArray("businessJobIds").add("job-1");
        scope.putArray("applicationIds");
        scope.putArray("moduleIds");

        ObjectNode job = document("job-1");
        job.put("businessUnitId", "bu-1");
        ObjectNode newApplication = document("new-app");
        newApplication.putArray("businessUnitIds").add("bu-1");
        ObjectNode newModule = document("new-module");
        newModule.put("applicationId", "new-app");
        newModule.putArray("businessUnitIds").add("bu-1");
        store.add("metiers", job);
        store.add("applications", newApplication);
        store.add("modules", newModule);

        assertThat(user.path("accessScope").path("applicationIds")).isEmpty();
        assertThat(user.path("accessScope").path("moduleIds")).isEmpty();
        assertThat(service.canSeeApplication(user, "new-app")).isTrue();
        assertThat(service.canSeeModule(user, newModule, false)).isTrue();
    }

    @Test
    void applicationIsVisibleToEveryJobOfAnAssignedBusinessUnit() {
        ObjectNode firstUser = userInJob("user-1", "job-1", "bu-1");
        ObjectNode secondUser = userInJob("user-2", "job-2", "bu-1");
        ObjectNode otherUnitUser = userInJob("user-3", "job-3", "bu-2");
        ObjectNode application = document("app-bu-1");
        application.putArray("businessUnitIds").add("bu-1");
        store.add("applications", application);

        assertThat(service.canSeeApplication(firstUser, "app-bu-1")).isTrue();
        assertThat(service.canSeeApplication(secondUser, "app-bu-1")).isTrue();
        assertThat(service.canSeeApplication(otherUnitUser, "app-bu-1")).isFalse();
    }

    private static ObjectNode userInJob(String userId, String jobId, String businessUnitId) {
        ObjectNode user = document(userId);
        user.put("role", "BUSINESS_USER");
        ObjectNode scope = user.putObject("accessScope");
        scope.put("global", false);
        scope.putArray("businessUnitIds").add(businessUnitId);
        scope.putArray("businessJobIds").add(jobId);
        scope.putArray("applicationIds");
        scope.putArray("moduleIds");
        return user;
    }

    private static ObjectNode document(String id) {
        ObjectNode value = JsonNodeFactory.instance.objectNode();
        value.put("id", id);
        value.put("status", "ACTIVE");
        return value;
    }

    private static JwtAuthenticationToken jwtAuthentication(String subject) {
        Jwt jwt = Jwt.withTokenValue("test-token")
            .header("alg", "none")
            .claim("sub", subject)
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plusSeconds(60))
            .build();
        return new JwtAuthenticationToken(jwt, List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }

    private static final class InMemoryResourceStore extends DoveResourceStore {

        private final Map<String, List<ObjectNode>> values = new HashMap<>();

        private InMemoryResourceStore() {
            super(null, null, null);
        }

        void add(String type, ObjectNode value) {
            values.computeIfAbsent(type, ignored -> new java.util.ArrayList<>()).add(value);
        }

        void replace(String type, ObjectNode value) {
            values.computeIfAbsent(type, ignored -> new java.util.ArrayList<>()).removeIf(
                existing -> existing.path("id").asText().equals(value.path("id").asText())
            );
            add(type, value);
        }

        @Override
        public List<ObjectNode> list(String type) {
            return values.getOrDefault(type, List.of());
        }

        @Override
        public Optional<ObjectNode> find(String type, String id) {
            return list(type).stream().filter(value -> id.equals(value.path("id").asText())).findFirst();
        }

        @Override
        public Optional<ObjectNode> findByExternalSubject(String type, String subject) {
            return list(type).stream().filter(value -> subject.equals(value.path("externalSubject").asText())).findFirst();
        }
    }
}
