<#import "template.ftl" as layout>
<@layout.registrationLayout displayMessage=!messagesPerField.existsError('email'); section>
	<#if section = "header">
		${msg("emailAuthTitle",realm.displayName)}
	<#elseif section = "form">
		<form id="kc-email-login-form" class="${properties.kcFormClass!}" action="${url.loginAction}" method="post">
			<div class="${properties.kcFormGroupClass!}">
				<div class="${properties.kcLabelWrapperClass!}">
					<label for="code" class="${properties.kcLabelClass!}">${msg("emailAuthLabel")}</label>
				</div>
				<div class="${properties.kcInputWrapperClass!}">
					<input type="text" id="code" name="code" class="${properties.kcInputClass!}" autofocus autocomplete="off"
						aria-invalid="<#if messagesPerField.existsError('email')>true</#if>"
					/>
					<#if messagesPerField.existsError('email')>
						<span id="input-error" class="${properties.kcInputErrorMessageClass!}" aria-live="polite">
								${kcSanitize(messagesPerField.getFirstError('email'))?no_esc}
						</span>
					</#if>
				</div>
			</div>
			<div class="${properties.kcFormGroupClass!} ${properties.kcFormSettingClass!}">
				<div id="kc-form-buttons" class="${properties.kcFormButtonsClass!}">
					<input class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonBlockClass!} ${properties.kcButtonLargeClass!}" type="submit" value="${msg("doLogIn")}"/>
				</div>
			</div>
		</form>
	<#elseif section = "info" >
		${msg("emailAuthInstruction")}
	</#if>
</@layout.registrationLayout>

