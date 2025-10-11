<#macro registrationLayout bodyClass="" displayInfo=false displayMessage=true displayRequiredFields=false>
<!DOCTYPE html>
<html class="${properties.kcHtmlClass!}">

<head>
    <meta charset="utf-8">
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="robots" content="noindex, nofollow">

    <#if properties.meta?has_content>
        <#list properties.meta?split(' ') as meta>
            <meta name="${meta?split('=')[0]}" content="${meta?split('=')[1]}"/>
        </#list>
    </#if>

    <title><#nested "title"></title>
    <link rel="icon" href="${url.resourcesPath}/img/favicon.ico" />

    <#if properties.stylesCommon?has_content>
        <#list properties.stylesCommon?split(' ') as style>
            <link href="${url.resourcesCommonPath}/${style}" rel="stylesheet" />
        </#list>
    </#if>
    <#if properties.styles?has_content>
        <#list properties.styles?split(' ') as style>
            <link href="${url.resourcesPath}/${style}" rel="stylesheet" />
        </#list>
    </#if>
    <#if properties.scripts?has_content>
        <#list properties.scripts?split(' ') as script>
            <script src="${url.resourcesPath}/${script}" type="text/javascript"></script>
        </#list>
    </#if>
    <#if scripts??>
        <#list scripts as script>
            <script src="${script}" type="text/javascript"></script>
        </#list>
    </#if>

    <style>
        /* Enhanced Shop Manager Theme Styles */
        :root {
            --sm-primary: #2563eb;
            --sm-primary-dark: #1d4ed8;
            --sm-secondary: #64748b;
            --sm-success: #16a34a;
            --sm-warning: #ea580c;
            --sm-error: #dc2626;
            --sm-surface: #ffffff;
            --sm-surface-dark: #f8fafc;
            --sm-text: #1e293b;
            --sm-text-light: #64748b;
            --sm-border: #e2e8f0;
            --sm-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
        }

        .floating-elements {
            position: fixed;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            pointer-events: none;
            z-index: 1;
        }

        .floating-element {
            position: absolute;
            opacity: 0.1;
            animation: float 6s ease-in-out infinite;
        }

        @keyframes float {
            0%, 100% { transform: translateY(0px) rotate(0deg); }
            50% { transform: translateY(-20px) rotate(180deg); }
        }
    </style>
</head>

<body class="${properties.kcBodyClass!}">
    <!-- Floating background elements -->
    <div class="floating-elements">
        <div class="floating-element" style="top: 10%; left: 10%; font-size: 24px;">🛍️</div>
        <div class="floating-element" style="top: 20%; right: 15%; font-size: 32px; animation-delay: -2s;">📊</div>
        <div class="floating-element" style="bottom: 30%; left: 20%; font-size: 28px; animation-delay: -4s;">💳</div>
        <div class="floating-element" style="bottom: 20%; right: 25%; font-size: 20px; animation-delay: -1s;">🏪</div>
    </div>

    <div class="${properties.kcLoginClass!}">
        <div id="kc-header" class="${properties.kcHeaderClass!}">
            <div id="kc-header-wrapper"
                 class="${properties.kcHeaderWrapperClass!}"><#nested "header"></div>
        </div>

        <div class="${properties.kcFormCardClass!}">
            <header class="${properties.kcFormHeaderClass!}">
                <#if realm.internationalizationEnabled && locale.supported?size gt 1>
                    <div class="${properties.kcLocaleMainClass!}" id="kc-locale">
                        <div id="kc-locale-wrapper" class="${properties.kcLocaleWrapperClass!}">
                            <div class="kc-dropdown" id="kc-locale-dropdown">
                                <a href="#" id="kc-current-locale-link">${locale.current}</a>
                                <ul class="${properties.kcLocaleListClass!}">
                                    <#list locale.supported as l>
                                        <li class="kc-dropdown-item"><a href="${l.url}">${l.label}</a></li>
                                    </#list>
                                </ul>
                            </div>
                        </div>
                    </div>
                </#if>
                <#if auth?has_content && auth.showUsername()>
                    <div class="${properties.kcFormGroupClass!}">
                        <div id="kc-username" class="${properties.kcInputWrapperClass!}">
                            <label id="kc-attempted-username">${auth.attemptedUsername}</label>
                            <a id="reset-login" href="${url.loginRestartFlowUrl}">
                                <div class="kc-login-tooltip">
                                    <i class="${properties.kcResetFlowIcon!}"></i>
                                    <span class="kc-tooltip-text">${msg("restartLoginTooltip")}</span>
                                </div>
                            </a>
                        </div>
                    </div>
                </#if>

                <div class="shop-manager-logo">
                    <h1 id="kc-page-title">
                        <span class="logo-icon">🏪</span>
                        <span class="logo-text">Shop Manager</span>
                    </h1>
                    <p class="subtitle">Retail Management Platform</p>
                    <p class="subtitle">Yes it now works...</p>
                </div>
            </header>

            <div id="kc-content">
                <div id="kc-content-wrapper">
                    <#-- App-initiated actions should not see warning messages about the need to complete the action -->
                    <#-- during login.                                                                               -->
                    <#if displayMessage && message?has_content && (message.type != 'warning' || !isAppInitiatedAction??)>
                        <div class="alert-${message.type} ${properties.kcAlertClass!} pf-m-<#if message.type = 'error'>danger<#else>${message.type}</#if>">
                            <div class="pf-c-alert__icon">
                                <#if message.type = 'success'><span class="${properties.kcFeedbackSuccessIcon!}"></span></#if>
                                <#if message.type = 'warning'><span class="${properties.kcFeedbackWarningIcon!}"></span></#if>
                                <#if message.type = 'error'><span class="${properties.kcFeedbackErrorIcon!}"></span></#if>
                                <#if message.type = 'info'><span class="${properties.kcFeedbackInfoIcon!}"></span></#if>
                            </div>
                            <span class="${properties.kcAlertTitleClass!}">${kcSanitize(message.summary)?no_esc}</span>
                        </div>
                    </#if>

                    <#nested "form">

                    <#if auth?has_content && auth.showTryAnotherWayLink() && showAnotherWayIfPresent>
                        <form id="kc-select-try-another-way-form" action="${url.loginAction}" method="post">
                            <div class="${properties.kcFormGroupClass!}">
                                <input type="hidden" name="tryAnotherWay" value="on"/>
                                <a href="#" id="try-another-way"
                                   onclick="document.forms['kc-select-try-another-way-form'].submit();return false;">${msg("doTryAnotherWay")}</a>
                            </div>
                        </form>
                    </#if>

                    <#if displayInfo>
                        <div id="kc-info" class="${properties.kcSignUpClass!}">
                            <div id="kc-info-wrapper" class="${properties.kcInfoAreaWrapperClass!}">
                                <#nested "info">
                            </div>
                        </div>
                    </#if>
                </div>
            </div>
        </div>
    </div>
</body>
</html>
</#macro>