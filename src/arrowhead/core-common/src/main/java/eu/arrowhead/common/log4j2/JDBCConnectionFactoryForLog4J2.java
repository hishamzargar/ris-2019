package eu.arrowhead.common.log4j2;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import eu.arrowhead.common.CommonConstants;

public class JDBCConnectionFactoryForLog4J2 {
	
	//=================================================================================================
	// members
	
	private static Properties props;
	private static DataSource dataSource;

	static {
		try {
			init();
		} catch (final IOException ex) {
			// this class' purpose to configure logging so in case of exceptions we can't use logging
			System.out.println(ex.getMessage()); //NOSONAR no logging at this point
			ex.printStackTrace(); //NOSONAR no logging at this point
		}
	}

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public static Connection getConnection() throws SQLException {
		if (dataSource == null) {
			final HikariConfig config = new HikariConfig();
			config.setJdbcUrl(props.getProperty(CommonConstants.DATABASE_URL));
			config.setUsername(props.getProperty(CommonConstants.DATABASE_USER));
			config.setPassword(props.getProperty(CommonConstants.DATABASE_PASSWORD));
			config.setDriverClassName(props.getProperty(CommonConstants.DATABASE_DRIVER_CLASS));
			
			dataSource = new HikariDataSource(config);
		}
		
		return dataSource.getConnection();
	}
	
	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	private static void init() throws IOException {
		InputStream propStream = null;
		File propertiesFile = new File(CommonConstants.APPLICATION_PROPERTIES);
		if (!propertiesFile.exists()) {
			propStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(CommonConstants.APPLICATION_PROPERTIES);
		} else {
			propStream = new FileInputStream(propertiesFile);
		}
		
		final Properties temp = new Properties();
		temp.load(propStream);

		props = temp;
	}
	
	//-------------------------------------------------------------------------------------------------
	private JDBCConnectionFactoryForLog4J2() {
		throw new UnsupportedOperationException();
	}
}