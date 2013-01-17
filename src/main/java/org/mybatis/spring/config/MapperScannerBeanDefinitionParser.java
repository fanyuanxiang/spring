package org.mybatis.spring.config;

import java.lang.annotation.Annotation;

import org.mybatis.spring.mapper.MapperFactoryBean;
import org.mybatis.spring.mapper.ClassPathMapperScanner;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.beans.factory.xml.XmlReaderContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 * A {#code BeanDefinitionParser} that handles the element scan of the MyBatis
 * namespace
 * 
 * @since 1.2.0
 * @see MapperFactoryBean
 * @see ClassPathMapperScanner
 */

public class MapperScannerBeanDefinitionParser implements BeanDefinitionParser {

  private static String ATTRIBUTE_BASE_PACKAGE = "base-package";
  private static String ATTRIBUTE_ANNOTATION = "annotation";
  private static String ATTRIBUTE_MARKER_INTERFACE = "marker-interface";
  private static String ATTRIBUTE_NAME_GENERATOR = "name-generator";
  private static String ATTRIBUTE_TEMPLATE_REF = "template-ref";
  private static String ATTRIBUTE_FACTORY_REF = "factory-ref";

  /**
   * {@inheritDoc}
   */
  public synchronized BeanDefinition parse(Element element, ParserContext parserContext) {
    ClassPathMapperScanner scanner = new ClassPathMapperScanner(parserContext.getRegistry(), false);
    ClassLoader classLoader = scanner.getResourceLoader().getClassLoader();
    XmlReaderContext readerContext = parserContext.getReaderContext();
    scanner.setResourceLoader(readerContext.getResourceLoader());
    try {
      String annotationClassName = element.getAttribute(ATTRIBUTE_ANNOTATION);
      if (StringUtils.hasText(annotationClassName)) {
        @SuppressWarnings("unchecked")
        Class<? extends Annotation> markerInterface = (Class<? extends Annotation>) classLoader.loadClass(annotationClassName);
        scanner.setAnnotationClass(markerInterface);
      }
      String markerInterfaceClassName = element.getAttribute(ATTRIBUTE_MARKER_INTERFACE);
      if (StringUtils.hasText(markerInterfaceClassName)) {
        Class<?> markerInterface = classLoader.loadClass(markerInterfaceClassName);
        scanner.setMarkerInterface(markerInterface);
      }
      String nameGeneratorClassName = element.getAttribute(ATTRIBUTE_NAME_GENERATOR);
      if (StringUtils.hasText(nameGeneratorClassName)) {
        Class<?> nameGeneratorClass = classLoader.loadClass(nameGeneratorClassName);
        BeanNameGenerator nameGenerator = BeanUtils.instantiateClass((Class<?>) nameGeneratorClass, BeanNameGenerator.class);
        scanner.setBeanNameGenerator(nameGenerator);
      }
    } catch (Exception ex) {
      readerContext.error(ex.getMessage(), readerContext.extractSource(element), ex.getCause());
    }
    String sqlSessionTemplateBeanName = element.getAttribute(ATTRIBUTE_TEMPLATE_REF);
    scanner.setSqlSessionTemplateBeanName(sqlSessionTemplateBeanName);
    String sqlSessionFactoryBeanName = element.getAttribute(ATTRIBUTE_FACTORY_REF);
    scanner.setSqlSessionFactoryBeanName(sqlSessionFactoryBeanName);
    scanner.registerFilters();
    String basePackage = element.getAttribute(ATTRIBUTE_BASE_PACKAGE);
    scanner.scan(StringUtils.tokenizeToStringArray(basePackage, ConfigurableApplicationContext.CONFIG_LOCATION_DELIMITERS));
    return null;
  }

}
