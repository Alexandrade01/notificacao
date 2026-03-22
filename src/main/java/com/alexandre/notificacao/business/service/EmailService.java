package com.alexandre.notificacao.business.service;

import com.alexandre.notificacao.business.dto.TarefasDTO;
import com.alexandre.notificacao.infrastructure.exceptions.EmailException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender javaMailSender;
    private final TemplateEngine templateEngine;

    @Value("${envio.email.remetente}")
    public String remetente;

    @Value("${envio.email.nomeRemetente}")
    public String nomeRemetente;

    //Serviço de envio de emails
    public void enviaEmail(TarefasDTO tarefa) {

        try{

            MimeMessage msg = javaMailSender.createMimeMessage();

            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(msg,true, StandardCharsets.UTF_8.name());

            //REMETENTE
            mimeMessageHelper.setFrom(new InternetAddress(remetente,nomeRemetente));

            //DESTINATARIO
            mimeMessageHelper.setTo(InternetAddress.parse(tarefa.getEmailUsuario()));

            //Assunto do email
            mimeMessageHelper.setSubject("Notificação de tarefa");


            //variaveis do corpo do email
            Context context = new Context();
            context.setVariable("nomeTarefa",tarefa.getNomeTarefa());
            context.setVariable("dataEvento", tarefa.getDataEvento() != null ? tarefa.getDataEvento().
                    format(java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy 'às' HH:mm")) : null);
            context.setVariable("descricao",tarefa.getDescricao());

            String template = templateEngine.process("notificacao",context);

            mimeMessageHelper.setText(template,true);

            //envia email
            javaMailSender.send(msg);

        } catch (MessagingException | UnsupportedEncodingException e) {
            throw new EmailException("Erro ao enviar o email " + e.getCause());
        }
    }

}
