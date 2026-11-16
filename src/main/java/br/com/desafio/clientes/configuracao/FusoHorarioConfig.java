package br.com.desafio.clientes.configuracao;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Configuration
public class FusoHorarioConfig {

    @Bean
    ZoneId fusoHorario(@Value("${aplicacao.fuso-horario}") String identificador) {
        return ZoneId.of(identificador);
    }

    @Bean
    Jackson2ObjectMapperBuilderCustomizer serializacaoDeDatas(ZoneId fusoHorario) {
        return builder -> {
            SimpleModule modulo = new SimpleModule();
            modulo.addSerializer(Instant.class, new JsonSerializer<>() {
                @Override
                public void serialize(Instant valor, JsonGenerator gerador, SerializerProvider serializers)
                        throws IOException {
                    String dataComFuso = DateTimeFormatter.ISO_OFFSET_DATE_TIME
                            .format(valor.atZone(fusoHorario));
                    gerador.writeString(dataComFuso);
                }
            });
            builder.modulesToInstall(modulo);
        };
    }
}
