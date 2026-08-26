<#import "template.ftl" as layout>
<@layout.registrationLayout displayMessage=false displayInfo=realm.password && realm.registrationAllowed && !registrationDisabled??; section>
  <#if section = "header">
    ${msg("loginAccountTitle")}
  <#elseif section = "form">
    <#if realm.password>
      <form id="kc-form-login" class="login-form" onsubmit="login.disabled = true; return true;" action="${url.loginAction}" method="post" novalidate>
        <#if !usernameHidden??>
          <label class="login-field" for="username">
            <span>${msg("usernameOrEmail")}</span>
            <span class="login-control">
              <svg class="login-control__icon" viewBox="0 0 24 24" aria-hidden="true"><circle cx="12" cy="8" r="4"></circle><path d="M4.5 21a7.5 7.5 0 0 1 15 0"></path></svg>
              <input id="username" name="username" value="${(login.username!'')}" type="text" autocomplete="username" autofocus placeholder="Votre login ou adresse email" aria-invalid="${messagesPerField.existsError('username','password')?string('true','false')}" required>
            </span>
          </label>
        </#if>

        <div class="login-field">
          <span class="login-field__heading">
            <label for="password">${msg("password")}</label>
            <#if realm.resetPasswordAllowed>
              <a href="${url.loginResetCredentialsUrl}">${msg("doForgotPassword")}</a>
            <#else>
              <a href="mailto:support.dove@orange-sonatel.com">${msg("doForgotPassword")}</a>
            </#if>
          </span>
          <span class="login-control">
            <svg class="login-control__icon" viewBox="0 0 24 24" aria-hidden="true"><rect x="5" y="10" width="14" height="11" rx="2"></rect><path d="M8 10V7a4 4 0 0 1 8 0v3"></path></svg>
            <input id="password" name="password" type="password" autocomplete="current-password" placeholder="Votre mot de passe" <#if usernameHidden??>autofocus</#if> aria-invalid="${messagesPerField.existsError('username','password')?string('true','false')}" required>
            <button class="password-toggle" type="button" data-dove-password-toggle aria-controls="password" aria-label="${msg('showPassword')}">
              <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M2.5 12s3.5-6 9.5-6 9.5 6 9.5 6-3.5 6-9.5 6-9.5-6-9.5-6z"></path><circle cx="12" cy="12" r="2.5"></circle></svg>
            </button>
          </span>
        </div>

        <#if messagesPerField.existsError('username','password')>
          <p class="login-error" role="alert">${kcSanitize(messagesPerField.getFirstError('username','password'))?no_esc}</p>
        </#if>

        <#if realm.rememberMe && !usernameHidden??>
          <label class="remember-choice" for="rememberMe">
            <input id="rememberMe" name="rememberMe" type="checkbox" <#if login.rememberMe??>checked</#if>>
            <span aria-hidden="true"></span>${msg("rememberMe")}
          </label>
        </#if>

        <input type="hidden" id="id-hidden-input" name="credentialId" <#if auth.selectedCredential?has_content>value="${auth.selectedCredential}"</#if>>
        <button class="login-primary" id="kc-login" name="login" type="submit">
          <span>${msg("doLogIn")}</span>
          <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M5 12h14M14 7l5 5-5 5"></path></svg>
        </button>
      </form>
    </#if>
  <#elseif section = "info">
    <#if realm.password && realm.registrationAllowed && !registrationDisabled??>
      <div id="kc-registration"><span>${msg("noAccount")} <a href="${url.registrationUrl}">${msg("doRegister")}</a></span></div>
    </#if>
  </#if>
</@layout.registrationLayout>

