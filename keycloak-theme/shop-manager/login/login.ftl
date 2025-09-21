<#import "template.ftl" as layout>
<@layout.registrationLayout displayMessage=!messagesPerField.existsError('username','password') displayInfo=realm.password && realm.registrationAllowed && !registrationDisabled??; section>
    <#if section = "header">
        <div id="kc-header">
            <div id="kc-header-wrapper">Shop Manager</div>
        </div>
    <#elseif section = "form">
    <div id="kc-form">
      <div id="kc-form-wrapper">
        <#if realm.password>
            <form id="kc-form-login" onsubmit="login.disabled = true; return true;" action="${url.loginAction}" method="post">
                <#if !usernameHidden??>
                    <div class="${properties.kcFormGroupClass!}">
                        <label for="username" class="${properties.kcLabelClass!}">
                            <#if !realm.loginWithEmailAllowed>${msg("username")}<#elseif !realm.registrationEmailAsUsername>${msg("usernameOrEmail")}<#else>${msg("email")}</#if>
                        </label>

                        <input tabindex="1" id="username" class="${properties.kcInputClass!}" name="username"
                               value="${(login.username!'')}"  type="text" autofocus autocomplete="off"
                               aria-invalid="<#if messagesPerField.existsError('username','password')>true</#if>"
                               placeholder="<#if !realm.loginWithEmailAllowed>${msg("username")}<#elseif !realm.registrationEmailAsUsername>${msg("usernameOrEmail")}<#else>admin@shopmanager.com</#if>"
                        />

                        <#if messagesPerField.existsError('username','password')>
                            <span id="input-error" class="${properties.kcInputErrorMessageClass!}" aria-live="polite">
                                    ${kcSanitize(messagesPerField.getFirstError('username','password'))?no_esc}
                            </span>
                        </#if>

                    </div>
                </#if>

                <div class="${properties.kcFormGroupClass!}">
                    <label for="password" class="${properties.kcLabelClass!}">${msg("password")}</label>

                    <input tabindex="2" id="password" class="${properties.kcInputClass!}" name="password"
                           type="password" autocomplete="current-password"
                           aria-invalid="<#if messagesPerField.existsError('username','password')>true</#if>"
                           placeholder="Enter your password"
                    />

                    <#if usernameHidden?? && messagesPerField.existsError('username','password')>
                        <span id="input-error" class="${properties.kcInputErrorMessageClass!}" aria-live="polite">
                                ${kcSanitize(messagesPerField.getFirstError('username','password'))?no_esc}
                        </span>
                    </#if>

                </div>

                <div class="${properties.kcFormGroupClass!} ${properties.kcFormSettingsClass!}">
                    <div id="kc-form-options">
                        <#if realm.rememberMe && !usernameHidden??>
                            <div class="checkbox">
                                <label>
                                    <#if login.rememberMe??>
                                        <input tabindex="3" id="rememberMe" name="rememberMe" type="checkbox" checked> ${msg("rememberMe")}
                                    <#else>
                                        <input tabindex="3" id="rememberMe" name="rememberMe" type="checkbox"> ${msg("rememberMe")}
                                    </#if>
                                </label>
                            </div>
                        </#if>
                        </div>
                        <div class="${properties.kcFormOptionsWrapperClass!}">
                            <#if realm.resetPasswordAllowed>
                                <span><a tabindex="5" href="${url.loginResetCredentialsUrl}">${msg("doForgotPassword")}</a></span>
                            </#if>
                        </div>

                  </div>

                  <div id="kc-form-buttons" class="${properties.kcFormGroupClass!}">
                      <input type="hidden" id="id-hidden-input" name="credentialId" <#if auth.selectedCredential?has_content>value="${auth.selectedCredential}"</#if>/>
                      <input tabindex="4" class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonBlockClass!} ${properties.kcButtonLargeClass!}" name="login" id="kc-login" type="submit" value="${msg("doLogIn")}"/>
                  </div>
            </form>
        </#if>
        </div>
    </div>

    <!-- Test Credentials Info for Development -->
    <#if realm.displayName == "Shop Manager">
    <div class="login-signup">
        <p style="font-size: 0.75rem; color: #64748b; margin-bottom: 0.5rem; font-weight: 600;">Test Credentials:</p>
        <div style="display: flex; flex-direction: column; gap: 0.25rem;">
            <button type="button" onclick="fillCredentials('admin@shopmanager.com', 'admin123')"
                    style="font-size: 0.75rem; color: #2563eb; background: none; border: none; cursor: pointer; text-align: left; padding: 0.25rem; border-radius: 0.25rem; transition: background-color 0.2s;">
                <span style="font-weight: 500;">Admin:</span> admin@shopmanager.com / admin123
            </button>
            <button type="button" onclick="fillCredentials('manager@shopmanager.com', 'manager123')"
                    style="font-size: 0.75rem; color: #2563eb; background: none; border: none; cursor: pointer; text-align: left; padding: 0.25rem; border-radius: 0.25rem; transition: background-color 0.2s;">
                <span style="font-weight: 500;">Manager:</span> manager@shopmanager.com / manager123
            </button>
            <button type="button" onclick="fillCredentials('employee@shopmanager.com', 'employee123')"
                    style="font-size: 0.75rem; color: #2563eb; background: none; border: none; cursor: pointer; text-align: left; padding: 0.25rem; border-radius: 0.25rem; transition: background-color 0.2s;">
                <span style="font-weight: 500;">Employee:</span> employee@shopmanager.com / employee123
            </button>
        </div>
        <p style="font-size: 0.7rem; color: #9ca3af; margin-top: 0.5rem;">
            Click to auto-fill credentials for testing
        </p>
    </div>

    <script>
        function fillCredentials(username, password) {
            document.getElementById('username').value = username;
            document.getElementById('password').value = password;
            document.getElementById('username').focus();
        }

        // Hover effects for credential buttons
        document.querySelectorAll('.login-signup button').forEach(btn => {
            btn.addEventListener('mouseenter', () => {
                btn.style.backgroundColor = '#f1f5f9';
            });
            btn.addEventListener('mouseleave', () => {
                btn.style.backgroundColor = 'transparent';
            });
        });
    </script>
    </#if>

    <#elseif section = "info" >
        <#if realm.password && realm.registrationAllowed && !registrationDisabled??>
            <div id="kc-registration-container">
                <div id="kc-registration">
                    <span>${msg("noAccount")} <a tabindex="6"
                                                 href="${url.registrationUrl}">${msg("doRegister")}</a></span>
                </div>
            </div>
        </#if>
    </#if>

</@layout.registrationLayout>