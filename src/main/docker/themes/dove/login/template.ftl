<#import "field.ftl" as field>
<#macro username>
  <#assign label>
    <#if !realm.loginWithEmailAllowed>${msg("username")}<#elseif !realm.registrationEmailAsUsername>${msg("usernameOrEmail")}<#else>${msg("email")}</#if>
  </#assign>
  <@field.group name="username" label=label>
    <div class="${properties.kcInputGroup}">
      <div class="${properties.kcInputGroupItemClass} ${properties.kcFill}">
        <span class="${properties.kcInputClass} ${properties.kcFormReadOnlyClass}">
          <input id="kc-attempted-username" value="${auth.attemptedUsername}" readonly>
        </span>
      </div>
      <div class="${properties.kcInputGroupItemClass}">
        <button id="reset-login" class="${properties.kcFormPasswordVisibilityButtonClass}" type="button"
                aria-label="${msg('restartLoginTooltip')}" onclick="location.href='${url.loginRestartFlowUrl}'">
          <span aria-hidden="true">&#8634;</span>
        </button>
      </div>
    </div>
  </@field.group>
</#macro>

<#macro registrationLayout bodyClass="" displayInfo=false displayMessage=true displayRequiredFields=false>
<!doctype html>
<html lang="${lang}"<#if realm.internationalizationEnabled> dir="${(locale.rtl)?then('rtl','ltr')}"</#if>>
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <meta name="color-scheme" content="light">
  <title>${title!"DOVE"}</title>
  <#if properties.styles?has_content>
    <#list properties.styles?split(' ') as style>
      <link href="${url.resourcesPath}/${style}" rel="stylesheet">
    </#list>
  </#if>
  <#if properties.scripts?has_content>
    <#list properties.scripts?split(' ') as script>
      <script src="${url.resourcesPath}/${script}" defer></script>
    </#list>
  </#if>
  <#if scripts??>
    <#list scripts as script><script src="${script}" defer></script></#list>
  </#if>
  <script type="module" src="${url.resourcesPath}/js/passwordVisibility.js"></script>
  <script type="module">
    <#outputformat "JavaScript">
    import { startSessionPolling } from ${(url.resourcesPath + "/js/authChecker.js")?c};
    startSessionPolling(${url.ssoLoginInOtherTabsUrl?c});
    </#outputformat>
  </script>
  <#if authenticationSession??>
    <script type="module">
      <#outputformat "JavaScript">
      import { checkAuthSession } from ${(url.resourcesPath + "/js/authChecker.js")?c};
      checkAuthSession(${authenticationSession.authSessionIdHash?c});
      </#outputformat>
    </script>
  </#if>
</head>
<body class="dove-auth-page ${bodyClass}" data-page-id="login-${pageId}">
  <main class="login-page">
    <section class="login-access" aria-labelledby="kc-page-title">
      <div class="login-access__content">
        <header class="login-heading">
          <span>RAVI DE VOUS REVOIR</span>
          <h1 id="kc-page-title"><#nested "header"></h1>
          <p>Retrouvez les connaissances utiles à votre métier, dans un espace sécurisé.</p>
        </header>

        <div class="dove-form-slot">
          <#if displayRequiredFields>
            <p class="required-fields"><span aria-hidden="true">*</span> ${msg("requiredFields")}</p>
          </#if>

          <#if auth?has_content && auth.showUsername() && !auth.showResetCredentials()>
            <div class="${properties.kcFormClass!} dove-attempted-username">
              <#nested "show-username">
              <@username />
            </div>
          </#if>

          <#if displayMessage && message?has_content && (message.type != 'warning' || !isAppInitiatedAction??)>
            <div class="login-message login-message--${message.type}" role="alert">
              ${kcSanitize(message.summary)?no_esc}
            </div>
          </#if>

          <#nested "form">

          <#if auth?has_content && auth.showTryAnotherWayLink()>
            <form id="kc-select-try-another-way-form" action="${url.loginAction}" method="post">
              <input type="hidden" name="tryAnotherWay" value="on">
              <button class="login-secondary" type="submit">${msg("doTryAnotherWay")}</button>
            </form>
          </#if>

          <#if switchOrganizationEnabled?? && switchOrganizationEnabled>
            <form id="kc-switch-organization-form" action="${url.loginAction}" method="post">
              <input type="hidden" name="switchOrganization" value="true">
              <button class="login-secondary" type="submit">${msg("doSwitchOrganization")}</button>
            </form>
          </#if>

          <#nested "socialProviders">
          <#if displayInfo>
            <div id="kc-info"><#nested "info"></div>
          </#if>
        </div>

        <footer class="login-support">
          Besoin d’aide&nbsp;? <a href="mailto:support.dove@orange-sonatel.com">Contactez le support</a>
        </footer>
      </div>
    </section>

    <aside class="login-visual" aria-label="Fonctionnalités principales de DOVE">
      <span class="visual-glow visual-glow--one" aria-hidden="true"></span>
      <span class="visual-glow visual-glow--two" aria-hidden="true"></span>
      <span class="visual-grid" aria-hidden="true"></span>

      <div class="orbit-stage">
        <span class="orbit-ring orbit-ring--outer" aria-hidden="true"></span>
        <span class="orbit-ring orbit-ring--inner" aria-hidden="true"></span>
        <span class="orbit-pulse orbit-pulse--one" aria-hidden="true"></span>
        <span class="orbit-pulse orbit-pulse--two" aria-hidden="true"></span>

        <div class="feature-node feature-node--search">
          <span aria-hidden="true"><svg viewBox="0 0 24 24"><circle cx="11" cy="11" r="6.5"></circle><path d="m16 16 4 4"></path></svg></span>
          <strong>Recherche</strong>
        </div>
        <div class="feature-node feature-node--file">
          <span aria-hidden="true"><svg viewBox="0 0 24 24"><path d="M6 3h8l4 4v14H6z"></path><path d="M14 3v5h5M9 12h6M9 16h6"></path></svg></span>
          <strong>Fiches pratiques</strong>
        </div>
        <div class="feature-node feature-node--video">
          <span aria-hidden="true"><svg viewBox="0 0 24 24"><rect x="3" y="5" width="18" height="14" rx="2"></rect><path d="m10 9 5 3-5 3z"></path></svg></span>
          <strong>Vidéos métiers</strong>
        </div>
        <div class="feature-node feature-node--faq">
          <span aria-hidden="true"><svg viewBox="0 0 24 24"><path d="M5 4h14v12H9l-4 4z"></path><path d="M9.8 9a2.2 2.2 0 1 1 3.8 1.5c-.8.7-1.6 1-1.6 2M12 15h.01"></path></svg></span>
          <strong>FAQ experte</strong>
        </div>

        <div class="dove-core">
          <span class="dove-core__halo" aria-hidden="true"></span>
          <div class="dove-core__logo">
            <img src="${url.resourcesPath}/img/logoDove.png" alt="DOVE">
          </div>
        </div>
      </div>
    </aside>
  </main>
</body>
</html>
</#macro>

