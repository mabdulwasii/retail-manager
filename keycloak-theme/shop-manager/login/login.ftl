<#import "template.ftl" as layout>
<@layout.registrationLayout displayMessage=!messagesPerField.existsError('username','password') displayInfo=realm.password && realm.registrationAllowed && !registrationDisabled??; section>
    <#if section = "header">
        <div id="kc-header">
            <div id="kc-header-wrapper">
                Welcome Back!
                <div style="font-size: 0.9rem; opacity: 0.9; margin-top: 0.5rem; font-weight: 500;">
                    Sign in to your Shop Manager account
                </div>
            </div>
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

                    <div class="password-input-wrapper">
                        <input tabindex="2" id="password" class="${properties.kcInputClass!}" name="password"
                               type="password" autocomplete="current-password"
                               aria-invalid="<#if messagesPerField.existsError('username','password')>true</#if>"
                               placeholder="Enter your password"
                               style="padding-right: 3rem;"
                        />
                        <button type="button" class="password-toggle" onclick="togglePassword()"
                                aria-label="Toggle password visibility" title="Show/Hide Password">
                            <span id="password-icon">👁️</span>
                        </button>
                    </div>

                    <#if usernameHidden?? && messagesPerField.existsError('username','password')>
                        <span id="input-error" class="${properties.kcInputErrorMessageClass!}" aria-live="polite">
                                ${kcSanitize(messagesPerField.getFirstError('username','password'))?no_esc}
                        </span>
                    </#if>

                </div>

                <div class="${properties.kcFormGroupClass!} ${properties.kcFormSettingsClass!}">
                    <div style="display: flex; justify-content: space-between; align-items: center; margin: 1.5rem 0;">
                        <div id="kc-form-options">
                            <#if realm.rememberMe && !usernameHidden??>
                                <div class="checkbox">
                                    <label>
                                        <#if login.rememberMe??>
                                            <input tabindex="3" id="rememberMe" name="rememberMe" type="checkbox" checked>
                                        <#else>
                                            <input tabindex="3" id="rememberMe" name="rememberMe" type="checkbox">
                                        </#if>
                                        <span>Remember me for 30 days</span>
                                    </label>
                                </div>
                            </#if>
                        </div>
                        <div class="${properties.kcFormOptionsWrapperClass!}">
                            <#if realm.resetPasswordAllowed>
                                <a tabindex="5" href="${url.loginResetCredentialsUrl}"
                                   style="font-size: 0.875rem; color: var(--shop-accent); text-decoration: none; font-weight: 500;">
                                    Forgot password?
                                </a>
                            </#if>
                        </div>
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
    <div class="login-signup" style="margin-top: 2rem; padding: 1.5rem; background: linear-gradient(135deg, #f8fafc 0%, #e2e8f0 100%); border-radius: 1rem; border: 1px solid rgba(59, 130, 246, 0.1);">
        <div style="text-align: center; margin-bottom: 1rem;">
            <div style="font-size: 0.9rem; color: #1e293b; font-weight: 700; margin-bottom: 0.25rem;">🧪 Development Mode</div>
            <p style="font-size: 0.75rem; color: #64748b; margin: 0;">Quick access test accounts</p>
        </div>

        <div style="display: grid; gap: 0.5rem;">
            <button type="button" onclick="fillCredentials('admin@shopmanager.com', 'admin123')"
                    class="credential-btn"
                    style="display: flex; align-items: center; justify-content: space-between; font-size: 0.8rem; color: #1e293b; background: linear-gradient(135deg, #ffffff 0%, #f8fafc 100%); border: 1px solid #e2e8f0; cursor: pointer; padding: 0.75rem 1rem; border-radius: 0.5rem; transition: all 0.2s ease; box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);">
                <div>
                    <span style="font-weight: 600; color: #3b82f6;">👤 Admin</span>
                    <div style="font-size: 0.7rem; color: #64748b; margin-top: 0.1rem;">admin@shopmanager.com</div>
                </div>
                <span style="font-size: 0.7rem; color: #10b981; font-weight: 500;">Click to fill</span>
            </button>

            <button type="button" onclick="fillCredentials('manager@shopmanager.com', 'manager123')"
                    class="credential-btn"
                    style="display: flex; align-items: center; justify-content: space-between; font-size: 0.8rem; color: #1e293b; background: linear-gradient(135deg, #ffffff 0%, #f8fafc 100%); border: 1px solid #e2e8f0; cursor: pointer; padding: 0.75rem 1rem; border-radius: 0.5rem; transition: all 0.2s ease; box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);">
                <div>
                    <span style="font-weight: 600; color: #3b82f6;">🏪 Manager</span>
                    <div style="font-size: 0.7rem; color: #64748b; margin-top: 0.1rem;">manager@shopmanager.com</div>
                </div>
                <span style="font-size: 0.7rem; color: #10b981; font-weight: 500;">Click to fill</span>
            </button>

            <button type="button" onclick="fillCredentials('employee@shopmanager.com', 'employee123')"
                    class="credential-btn"
                    style="display: flex; align-items: center; justify-content: space-between; font-size: 0.8rem; color: #1e293b; background: linear-gradient(135deg, #ffffff 0%, #f8fafc 100%); border: 1px solid #e2e8f0; cursor: pointer; padding: 0.75rem 1rem; border-radius: 0.5rem; transition: all 0.2s ease; box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);">
                <div>
                    <span style="font-weight: 600; color: #3b82f6;">👥 Employee</span>
                    <div style="font-size: 0.7rem; color: #64748b; margin-top: 0.1rem;">employee@shopmanager.com</div>
                </div>
                <span style="font-size: 0.7rem; color: #10b981; font-weight: 500;">Click to fill</span>
            </button>
        </div>

        <div style="text-align: center; margin-top: 1rem; padding-top: 1rem; border-top: 1px solid rgba(59, 130, 246, 0.1);">
            <p style="font-size: 0.7rem; color: #9ca3af; margin: 0;">
                ⚠️ Development credentials only - disabled in production
            </p>
        </div>
    </div>

    <script>
        // Password visibility toggle
        function togglePassword() {
            const passwordInput = document.getElementById('password');
            const passwordIcon = document.getElementById('password-icon');

            if (passwordInput.type === 'password') {
                passwordInput.type = 'text';
                passwordIcon.textContent = '🙈';
                passwordIcon.parentElement.setAttribute('title', 'Hide Password');
            } else {
                passwordInput.type = 'password';
                passwordIcon.textContent = '👁️';
                passwordIcon.parentElement.setAttribute('title', 'Show Password');
            }
        }

        // Auto-fill credentials
        function fillCredentials(username, password) {
            document.getElementById('username').value = username;
            document.getElementById('password').value = password;
            document.getElementById('username').focus();

            // Show feedback animation
            const usernameField = document.getElementById('username');
            const passwordField = document.getElementById('password');

            usernameField.style.borderColor = '#10b981';
            passwordField.style.borderColor = '#10b981';

            setTimeout(() => {
                usernameField.style.borderColor = '';
                passwordField.style.borderColor = '';
            }, 1000);
        }

        // Enhanced button hover effects
        document.addEventListener('DOMContentLoaded', function() {
            // Credential button hover effects
            document.querySelectorAll('.credential-btn').forEach(btn => {
                btn.addEventListener('mouseenter', () => {
                    btn.style.transform = 'translateY(-2px)';
                    btn.style.boxShadow = '0 8px 25px -8px rgba(59, 130, 246, 0.3)';
                    btn.style.borderColor = '#3b82f6';
                });
                btn.addEventListener('mouseleave', () => {
                    btn.style.transform = '';
                    btn.style.boxShadow = '';
                    btn.style.borderColor = '';
                });
            });

            // Form validation feedback
            const form = document.getElementById('kc-form-login');
            if (form) {
                form.addEventListener('submit', function(e) {
                    const submitBtn = document.getElementById('kc-login');
                    submitBtn.innerHTML = '🔐 Signing In...';
                    submitBtn.style.background = 'linear-gradient(135deg, #64748b 0%, #475569 100%)';
                });
            }

            // Keyboard shortcuts
            document.addEventListener('keydown', function(e) {
                // Ctrl/Cmd + Enter to submit
                if ((e.ctrlKey || e.metaKey) && e.key === 'Enter') {
                    const submitBtn = document.getElementById('kc-login');
                    if (submitBtn) submitBtn.click();
                }
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