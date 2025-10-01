{{/*
Expand the name of the chart.
*/}}
{{- define "shop-manager.name" -}}
{{- default (.Chart.Name | default "shop-manager") (and .Values .Values.nameOverride) | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Get the application name - uses global.appName if set, otherwise release name
*/}}
{{- define "shop-manager.appName" -}}
{{- if .Values -}}
  {{- if .Values.global -}}
    {{- if .Values.global.appName -}}
      {{- .Values.global.appName -}}
    {{- else -}}
      {{- if .Release -}}
        {{- .Release.Name -}}
      {{- else -}}
        {{- "shop-manager" -}}
      {{- end -}}
    {{- end -}}
  {{- else -}}
    {{- if .Release -}}
      {{- .Release.Name -}}
    {{- else -}}
      {{- "shop-manager" -}}
    {{- end -}}
  {{- end -}}
{{- else -}}
  {{- if .Release -}}
    {{- .Release.Name -}}
  {{- else -}}
    {{- "shop-manager" -}}
  {{- end -}}
{{- end -}}
{{- end }}

{{/*
Create a default fully qualified app name.
We truncate at 63 chars because some Kubernetes name fields are limited to this (by the DNS naming spec).
If release name contains chart name it will be used as a full name.
*/}}
{{- define "shop-manager.fullname" -}}
{{- if and .Values .Values.fullnameOverride }}
{{- .Values.fullnameOverride | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- $appName := include "shop-manager.appName" . }}
{{- $appName | trunc 63 | trimSuffix "-" }}
{{- end }}
{{- end }}

{{/*
Create chart name and version as used by the chart label.
*/}}
{{- define "shop-manager.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Common labels
*/}}
{{- define "shop-manager.labels" -}}
helm.sh/chart: {{ include "shop-manager.chart" . }}
{{ include "shop-manager.selectorLabels" . }}
{{- if .Chart.AppVersion }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
{{- end }}
{{- if .Release }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- else }}
app.kubernetes.io/managed-by: Helm
{{- end }}
{{- end }}

{{/*
Selector labels
*/}}
{{- define "shop-manager.selectorLabels" -}}
app.kubernetes.io/name: {{ include "shop-manager.name" . }}
{{- if .Release }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- else }}
app.kubernetes.io/instance: {{ include "shop-manager.appName" . }}
{{- end }}
{{- end }}

{{/*
Generate frontend hostname
*/}}
{{- define "shop-manager.frontend.hostname" -}}
{{- $appName := include "shop-manager.appName" . -}}
{{- $domain := "shop-manager.local" -}}
{{- if .Values -}}
  {{- if .Values.global -}}
    {{- if .Values.global.domain -}}
      {{- $domain = .Values.global.domain -}}
    {{- end -}}
  {{- end -}}
{{- end -}}
{{- if eq $appName "shop-manager" }}
{{- $domain }}
{{- else }}
{{- printf "%s.%s" $appName $domain }}
{{- end }}
{{- end }}

{{/*
Generate backend API hostname
*/}}
{{- define "shop-manager.backend.hostname" -}}
{{- $appName := include "shop-manager.appName" . -}}
{{- $domain := "" -}}
{{- if .Values -}}
  {{- if .Values.global -}}
    {{- if .Values.global.domain -}}
      {{- $domain = .Values.global.domain -}}
    {{- else -}}
      {{- $domain = "shop-manager.local" -}}
    {{- end -}}
  {{- else -}}
    {{- $domain = "shop-manager.local" -}}
  {{- end -}}
{{- else -}}
  {{- $domain = "shop-manager.local" -}}
{{- end -}}
{{- if eq $appName "shop-manager" }}
{{- printf "api.%s" $domain }}
{{- else }}
{{- printf "api.%s.%s" $appName $domain }}
{{- end }}
{{- end }}

{{/*
Generate Keycloak hostname
*/}}
{{- define "shop-manager.keycloak.hostname" -}}
{{- $appName := include "shop-manager.appName" . -}}
{{- $domain := "" -}}
{{- if .Values -}}
  {{- if .Values.global -}}
    {{- if .Values.global.domain -}}
      {{- $domain = .Values.global.domain -}}
    {{- else -}}
      {{- $domain = "shop-manager.local" -}}
    {{- end -}}
  {{- else -}}
    {{- $domain = "shop-manager.local" -}}
  {{- end -}}
{{- else -}}
  {{- $domain = "shop-manager.local" -}}
{{- end -}}
{{- if eq $appName "shop-manager" }}
{{- printf "auth.%s" $domain }}
{{- else }}
{{- printf "auth.%s.%s" $appName $domain }}
{{- end }}
{{- end }}

{{/*
Generate certificate name for frontend
*/}}
{{- define "shop-manager.frontend.certName" -}}
{{- printf "%s-frontend-tls" (include "shop-manager.appName" .) }}
{{- end }}

{{/*
Generate certificate name for backend
*/}}
{{- define "shop-manager.backend.certName" -}}
{{- printf "%s-backend-tls" (include "shop-manager.appName" .) }}
{{- end }}

{{/*
Generate certificate name for Keycloak
*/}}
{{- define "shop-manager.keycloak.certName" -}}
{{- printf "%s-keycloak-tls" (include "shop-manager.appName" .) }}
{{- end }}

{{/*
Generate PostgreSQL connection string
*/}}
{{- define "shop-manager.postgresql.host" -}}
{{- $appName := include "shop-manager.appName" . -}}
{{- printf "%s-postgresql" $appName }}
{{- end }}

{{/*
Generate Keycloak PostgreSQL connection string
*/}}
{{- define "shop-manager.keycloak.postgresql.host" -}}
{{- $appName := include "shop-manager.appName" . -}}
{{- printf "%s-keycloak-postgresql" $appName }}
{{- end }}

{{/*
Generate main PostgreSQL name
*/}}
{{- define "shop-manager.postgresql.name" -}}
{{- $appName := include "shop-manager.appName" . -}}
{{- printf "%s-postgresql" $appName }}
{{- end }}

{{/*
Generate JWK Set URI for backend (internal service communication)
*/}}
{{- define "shop-manager.keycloak.jwkSetUri" -}}
{{- $appName := include "shop-manager.appName" . -}}
{{- printf "http://%s-keycloak:80/realms/shop-manager/protocol/openid-connect/certs" $appName }}
{{- end }}

{{/*
Generate frontend API base URL
*/}}
{{- define "shop-manager.frontend.apiBaseUrl" -}}
{{- printf "https://%s/api" (include "shop-manager.backend.hostname" .) }}
{{- end }}

{{/*
Generate frontend Keycloak URL
*/}}
{{- define "shop-manager.frontend.keycloakUrl" -}}
{{- printf "https://%s" (include "shop-manager.keycloak.hostname" .) }}
{{- end }}

{{/*
Generate Keycloak issuer URI
*/}}
{{- define "shop-manager.keycloak.issuerUri" -}}
{{- printf "https://%s/realms/shop-manager" (include "shop-manager.keycloak.hostname" .) }}
{{- end }}

{{/*
Generate Keycloak auth server URL
*/}}
{{- define "shop-manager.keycloak.authServerUrl" -}}
{{- printf "https://%s" (include "shop-manager.keycloak.hostname" .) }}
{{- end }}

{{/*
Generate configuration map name
*/}}
{{- define "shop-manager.configMapName" -}}
{{- printf "%s-config" (include "shop-manager.appName" .) }}
{{- end }}

{{/*
Generate secret name
*/}}
{{- define "shop-manager.secretName" -}}
{{- printf "%s-secrets" (include "shop-manager.appName" .) }}
{{- end }}

{{/*
Generate theme ConfigMap name
*/}}
{{- define "shop-manager.themeConfigMapName" -}}
{{- printf "%s-keycloak-theme" (include "shop-manager.appName" .) }}
{{- end }}

{{/*
Generate dynamic Keycloak theme name
*/}}
{{- define "shop-manager.keycloak.themeName" -}}
{{- printf "%s-theme" (include "shop-manager.appName" .) }}
{{- end }}

{{/*
Generate dynamic theme CSS file name
*/}}
{{- define "shop-manager.keycloak.themeCssName" -}}
{{- printf "%s-theme.css" (include "shop-manager.appName" .) }}
{{- end }}

{{/*
Generate cert-installer job name
*/}}
{{- define "shop-manager.certInstallerJobName" -}}
{{- printf "%s-cert-installer" (include "shop-manager.appName" .) }}
{{- end }}

{{/*
Get platform name for branding
*/}}
{{- define "shop-manager.platformName" -}}
{{- .Values.branding.platformName | default "Shop Manager" }}
{{- end }}

{{/*
Get company name for branding
*/}}
{{- define "shop-manager.companyName" -}}
{{- .Values.branding.companyName | default "Your Company" }}
{{- end }}

{{/*
Get platform description
*/}}
{{- define "shop-manager.platformDescription" -}}
{{- .Values.branding.platformDescription | default "Retail Management Platform" }}
{{- end }}

{{/*
Generate image name with registry prefix if specified
*/}}
{{- define "shop-manager.image" -}}
{{- $image := .image -}}
{{- $registry := "" -}}
{{- if .Values -}}
  {{- if .Values.global -}}
    {{- if .Values.global.imageRegistry -}}
      {{- $registry = .Values.global.imageRegistry -}}
    {{- end -}}
  {{- end -}}
{{- end -}}
{{- if $registry }}
{{- printf "%s/%s" $registry $image }}
{{- else }}
{{- $image }}
{{- end }}
{{- end }}

{{/*
Validate configuration
*/}}
{{- define "shop-manager.validate" -}}
{{- if .Values -}}
  {{- if .Values.global -}}
    {{- if not .Values.global.domain -}}
      {{- fail "global.domain is required when global section is defined" -}}
    {{- end -}}
  {{- end -}}
{{- end -}}
{{- end }}