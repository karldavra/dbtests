package com.davra.ledgertests.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

@Configuration
@EnableJpaRepositories(basePackages = "com.davra.ledgertests")
public class DataSourceConfig {

  @Autowired
  private Environment env;

  @Bean
  public DataSource dataSource() {
    final DriverManagerDataSource dataSource = new DriverManagerDataSource();
    dataSource.setUrl(env.getProperty("spring.datasource.url"));
    dataSource.setDriverClassName(env.getProperty("spring.datasource.driverClassName"));
    dataSource.setUsername(env.getProperty("spring.datasource.user"));
    dataSource.setPassword(env.getProperty("spring.datasource.password"));
    return dataSource;
  }

  @Bean
  public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
    final LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
    em.setDataSource(dataSource());
    em.setPackagesToScan(new String[] { "com.davra.ledgertests" });
    em.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
    em.setJpaProperties(additionalProperties());
    return em;
  }

  final Properties additionalProperties() {
    final Properties hibernateProperties = new Properties();
    if (env.getProperty("hibernate.hbm2ddl.auto") != null) {
      hibernateProperties.setProperty("hibernate.hbm2ddl.auto", env.getProperty("hibernate.hbm2ddl.auto"));
    }
    if (env.getProperty("hibernate.dialect") != null) {
      hibernateProperties.setProperty("hibernate.dialect", env.getProperty("hibernate.dialect"));
    }
    if (env.getProperty("hibernate.show_sql") != null) {
      hibernateProperties.setProperty("hibernate.show_sql", env.getProperty("hibernate.show_sql"));
    }
    return hibernateProperties;
  }

  private static void zipFile(File fileToZip, String fileName, ZipOutputStream zipOut) throws IOException {
    if (fileToZip.isHidden()) {
      return;
    }
    if (fileToZip.isDirectory()) {
      if (fileName.endsWith("/")) {
        zipOut.putNextEntry(new ZipEntry(fileName));
        zipOut.closeEntry();
      } else {
        zipOut.putNextEntry(new ZipEntry(fileName + "/"));
        zipOut.closeEntry();
      }
      File[] children = fileToZip.listFiles();
      for (File childFile : children) {
        zipFile(childFile, fileName + "/" + childFile.getName(), zipOut);
      }
      return;
    }
    FileInputStream fis = new FileInputStream(fileToZip);
    ZipEntry zipEntry = new ZipEntry(fileName);
    zipOut.putNextEntry(zipEntry);
    byte[] bytes = new byte[1024];
    int length;
    while ((length = fis.read(bytes)) >= 0) {
      zipOut.write(bytes, 0, length);
    }
    fis.close();
  }

  public void doBackUp(Long offset) {
    try {
      String sourceFile = "demo";
      FileOutputStream fos;
      fos = new FileOutputStream("BKUP_" + offset.toString() + ".zip");
      ZipOutputStream zipOut = new ZipOutputStream(fos);
      File fileToZip = new File(sourceFile);
      zipFile(fileToZip, fileToZip.getName(), zipOut);
      zipOut.close();
      fos.close();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  public void doRestore(Long offset) {
    String fileZip = "BKUP_" + offset.toString() + ".zip";
    File destDir = new File("demo");
    try {
      byte[] buffer = new byte[1024];
      ZipInputStream zis = new ZipInputStream(new FileInputStream(fileZip));
      ZipEntry zipEntry = zis.getNextEntry();
      while (zipEntry != null) {
        // ...
      }

      zis.closeEntry();
      zis.close();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

}
