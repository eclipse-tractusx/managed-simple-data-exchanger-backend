package org.eclipse.tractusx.sde.configuration;

import org.eclipse.tractusx.sde.pcfexchange.enums.PCFTypeEnumFormator;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class EnumMappingAppConfig implements WebMvcConfigurer {

   private PCFTypeEnumFormator pcfEnumFormatter() {
        return new PCFTypeEnumFormator();
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addFormatter(this.pcfEnumFormatter());
    }

}
