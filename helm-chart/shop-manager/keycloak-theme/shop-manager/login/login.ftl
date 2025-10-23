<#import "template.ftl" as layout>
<@layout.registrationLayout displayMessage=!messagesPerField.existsError('username','password') displayInfo=realm.password && realm.registrationAllowed && !registrationDisabled??; section>
    <#if section = "title">
        {{ .Values.branding.platformName }}
    <#elseif section = "header">
        <#-- Header is handled in template.ftl -->
    <#elseif section = "form">
        <#if realm.password>
            <form id="kc-form-login" onsubmit="login.disabled = true; return true;" action="${url.loginAction}" method="post">
                <div class="form-group">
                    <#if !usernameHidden??>
                        <label for="username">
                            <#if !realm.loginWithEmailAllowed>${msg("username")}<#elseif !realm.registrationEmailAsUsername>${msg("usernameOrEmail")}<#else>${msg("email")}</#if>
                        </label>
                        
                        <input 
                            id="username" 
                            name="username" 
                            type="text" 
                            value="${(login.username!'')}" 
                            placeholder="<#if !realm.loginWithEmailAllowed>${msg("username")}<#elseif !realm.registrationEmailAsUsername>${msg("usernameOrEmail")}<#else>${msg("email")}</#if>" 
                            autofocus 
                            autocomplete="username" 
                            aria-invalid="<#if messagesPerField.existsError('username','password')>true</#if>"
                        />
                        
                        <#if messagesPerField.existsError('username','password')>
                            <div class="error-message">${kcSanitize(messagesPerField.getFirstError('username','password'))?no_esc}</div>
                        </#if>
                    </#if>
                </div>

                <div class="form-group">
                    <div class="password-label-group">
                        <label for="password">${msg("password")}</label>
                        <#if realm.resetPasswordAllowed>
                            <a href="${url.loginResetCredentialsUrl}" class="forgot-password">${msg("doForgotPassword")}</a>
                        </#if>
                    </div>
                    
                    <div class="password-wrapper">
                        <input 
                            id="password" 
                            name="password" 
                            type="password" 
                            placeholder="${msg("password")}" 
                            autocomplete="current-password"
                            aria-invalid="<#if messagesPerField.existsError('username','password')>true</#if>"
                        />
                        <button type="button" class="password-toggle" id="password-toggle" tabindex="-1">
                            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="show-password"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path><circle cx="12" cy="12" r="3"></circle></svg>
                            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="hide-password"><path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"></path><line x1="1" y1="1" x2="23" y2="23"></line></svg>
                        </button>
                    </div>
                    
                    <#if usernameHidden?? && messagesPerField.existsError('username','password')>
                        <div class="error-message">${kcSanitize(messagesPerField.getFirstError('username','password'))?no_esc}</div>
                    </#if>
                </div>

                <div class="checkbox-container">
                    <#if realm.rememberMe && !usernameHidden??>
                        <label>
                            <input type="checkbox" id="rememberMe" name="rememberMe" <#if login.rememberMe??>checked</#if>>
                            ${msg("rememberMe")}
                        </label>
                    </#if>
                </div>

                <div class="form-group">
                    <button class="submit" type="submit" name="login" id="kc-login">
                        ${msg("doLogIn")}
                    </button>
                </div>
            </form>
        </#if>
    <#elseif section = "info">
        <#if realm.password && realm.registrationAllowed && !registrationDisabled??>
            <div>
                ${msg("noAccount")} <a tabindex="6" href="${url.registrationUrl}">${msg("doRegister")}</a>
            </div>
        </#if>
    </#if>
</@layout.registrationLayout>