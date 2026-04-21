package com.dxc.auth.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${app.email.from}")
    private String fromEmail;

    @Value("${app.email.from-name}")
    private String fromName;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    private void sendEmail(String toEmail, String subject, String htmlContent) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(toEmail);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);

        try {
            helper.setFrom(fromEmail, fromName);
        } catch (java.io.UnsupportedEncodingException e) {
            helper.setFrom(fromEmail);
        }

        mailSender.send(message);
    }

    public void sendVerificationEmail(String toEmail, String firstName, String verificationToken) throws MessagingException {
        log.info("Envoi email de vérification à: {}", toEmail);

        String verificationLink = frontendUrl + "/html/verify-email.html?token=" + verificationToken;
        String subject = "Vérifiez votre compte - DXC Technology";

        String htmlContent = String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; background-color: #f4f4f4; margin: 0; padding: 0; }
                    .container { max-width: 600px; margin: 40px auto; background: white; border-radius: 16px; overflow: hidden; box-shadow: 0 4px 12px rgba(0,0,0,0.1); }
                    .header { background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); padding: 40px; text-align: center; color: white; }
                    .header h1 { margin: 0; font-size: 28px; }
                    .content { padding: 40px; }
                    .content h2 { color: #333; margin-top: 0; }
                    .content p { color: #666; line-height: 1.6; }
                    .button { display: inline-block; padding: 16px 32px; background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); color: white; text-decoration: none; border-radius: 8px; font-weight: bold; margin: 20px 0; }
                    .link-box { background: #f9f9f9; padding: 16px; border-radius: 8px; word-break: break-all; font-size: 12px; color: #666; margin: 20px 0; }
                    .footer { background: #f9f9f9; padding: 20px; text-align: center; color: #999; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Bienvenue chez DXC Technology!</h1>
                    </div>
                    <div class="content">
                        <h2>Bonjour %s,</h2>
                        <p>Merci de vous être inscrit sur le portail DXC Technology !</p>
                        <p>Pour activer votre compte, veuillez cliquer sur le bouton ci-dessous :</p>
                        <center>
                            <a href="%s" class="button">Activer mon compte</a>
                        </center>
                        <p>Ou copiez ce lien dans votre navigateur :</p>
                        <div class="link-box">%s</div>
                    </div>
                    <div class="footer">
                        <p>DXC Technology - Portail RH</p>
                        <p>Cet email a été envoyé automatiquement, merci de ne pas y répondre.</p>
                    </div>
                </div>
            </body>
            </html>
            """, firstName, verificationLink, verificationLink);

        sendEmail(toEmail, subject, htmlContent);
    }

    public void sendWelcomeEmail(String toEmail, String firstName) throws MessagingException {
        log.info("Envoi email de bienvenue à: {}", toEmail);

        String subject = "Compte activé - Bienvenue chez DXC!";

        String htmlContent = String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; background-color: #f4f4f4; margin: 0; padding: 0; }
                    .container { max-width: 600px; margin: 40px auto; background: white; border-radius: 16px; overflow: hidden; box-shadow: 0 4px 12px rgba(0,0,0,0.1); }
                    .header { background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); padding: 40px; text-align: center; color: white; }
                    .header h1 { margin: 0; font-size: 28px; }
                    .content { padding: 40px; }
                    .content h2 { color: #333; margin-top: 0; }
                    .content p { color: #666; line-height: 1.6; }
                    .button { display: inline-block; padding: 16px 32px; background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); color: white; text-decoration: none; border-radius: 8px; font-weight: bold; margin: 20px 0; }
                    .footer { background: #f9f9f9; padding: 20px; text-align: center; color: #999; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Compte activé avec succès!</h1>
                    </div>
                    <div class="content">
                        <h2>Félicitations %s,</h2>
                        <p>Votre compte DXC Technology a été activé avec succès!</p>
                        <p>Vous pouvez maintenant vous connecter et profiter de tous nos services.</p>
                        <center>
                            <a href="%s/login.html" class="button">Accéder au portail</a>
                        </center>
                    </div>
                    <div class="footer">
                        <p>DXC Technology - Portail RH</p>
                        <p>Cet email a été envoyé automatiquement, merci de ne pas y répondre.</p>
                    </div>
                </div>
            </body>
            </html>
            """, firstName, frontendUrl);

        sendEmail(toEmail, subject, htmlContent);
    }

    public void sendOtpEmail(String toEmail, String firstName, String otpCode) throws MessagingException {
        log.info("Envoi email OTP à: {}", toEmail);

        String subject = "Code de vérification DXC Technology";

        String htmlContent = String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; background-color: #f4f4f4; margin: 0; padding: 0; }
                    .container { max-width: 600px; margin: 40px auto; background: white; border-radius: 16px; overflow: hidden; box-shadow: 0 4px 12px rgba(0,0,0,0.1); }
                    .header { background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); padding: 40px; text-align: center; color: white; }
                    .header h1 { margin: 0; font-size: 28px; }
                    .content { padding: 40px; }
                    .content h2 { color: #333; margin-top: 0; }
                    .content p { color: #666; line-height: 1.6; }
                    .otp-box { background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); color: white; padding: 20px; text-align: center; border-radius: 8px; margin: 30px 0; font-size: 32px; font-weight: bold; letter-spacing: 8px; }
                    .warning { background: #fff3cd; border-left: 4px solid #ffc107; padding: 16px; margin: 20px 0; color: #856404; }
                    .footer { background: #f9f9f9; padding: 20px; text-align: center; color: #999; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Code de connexion</h1>
                    </div>
                    <div class="content">
                        <h2>Bonjour %s,</h2>
                        <p>Voici votre code de vérification pour vous connecter au portail DXC :</p>
                        <div class="otp-box">%s</div>
                        <div class="warning">
                            <strong>⏰ Ce code expire dans 5 minutes</strong>
                        </div>
                        <p>Entrez ce code dans la page de connexion pour finaliser votre authentification.</p>
                        <p><strong>Important :</strong> Si vous n'avez pas demandé ce code, ignorez cet email.</p>
                    </div>
                    <div class="footer">
                        <p>DXC Technology - Portail RH</p>
                        <p>Cet email a été envoyé automatiquement, merci de ne pas y répondre.</p>
                    </div>
                </div>
            </body>
            </html>
            """, firstName, otpCode);

        sendEmail(toEmail, subject, htmlContent);
    }

    public void sendPasswordResetEmail(String toEmail, String firstName, String resetToken) throws MessagingException {
        log.info("Envoi email reset password à: {}", toEmail);

        String resetLink = frontendUrl + "/html/reset-password.html?token=" + resetToken;
        String subject = "Réinitialisation de votre mot de passe - DXC Technology";

        String htmlContent = "<!DOCTYPE html>" +
            "<html>" +
            "<head>" +
            "<meta charset='UTF-8'>" +
            "</head>" +
            "<body style='font-family: Arial, sans-serif; background-color: #f5f5f5; padding: 20px;'>" +
            "<div style='max-width: 600px; margin: 0 auto; background: white; border-radius: 10px; padding: 30px; box-shadow: 0 2px 10px rgba(0,0,0,0.1);'>" +
            "<h2 style='color: #1A1F71; margin-bottom: 20px;'>Réinitialisation de mot de passe</h2>" +
            "<p style='color: #333; font-size: 16px; line-height: 1.6;'>Bonjour <strong>" + firstName + "</strong>,</p>" +
            "<p style='color: #333; font-size: 16px; line-height: 1.6;'>Vous avez demandé à réinitialiser votre mot de passe DXC Technology.</p>" +
            "<p style='color: #333; font-size: 16px; line-height: 1.6; margin: 30px 0;'>Cliquez sur le bouton ci-dessous :</p>" +
            "<div style='text-align: center; margin: 30px 0;'>" +
            "<a href='" + resetLink + "' style='display: inline-block; padding: 15px 40px; background-color: #1A1F71; color: white; text-decoration: none; border-radius: 5px; font-weight: bold; font-size: 16px;'>Réinitialiser mon mot de passe</a>" +
            "</div>" +
            "<p style='color: #666; font-size: 14px; margin-top: 30px;'>Ou copiez ce lien :</p>" +
            "<p style='background-color: #f5f5f5; padding: 15px; border-radius: 5px; word-break: break-all; color: #1A1F71; font-size: 13px;'>" + resetLink + "</p>" +
            "<p style='color: #999; font-size: 12px; margin-top: 30px; padding-top: 20px; border-top: 1px solid #eee;'>Ce lien expire dans 24 heures.</p>" +
            "<p style='color: #999; font-size: 12px; margin-top: 10px;'>DXC Technology - Portail RH</p>" +
            "</div>" +
            "</body>" +
            "</html>";

        sendEmail(toEmail, subject, htmlContent);
    }
}