package org.mongodb.datasource;

import com.mongodb.MongoClient;

/**
 * https://github.com/pozil/mongodb-jndi-datasource
 *
 * Bean holding an open MongoClient used as a pooled datasource and its configuration
 *
 * @author Philippe Ozil
 */
public class MongoDatasource
{
	private final MongoClient client;

	protected MongoDatasource(final MongoClient client)
	{
		this.client = client;
	}

	/**
	 * Retrieves a client from this datasource
	 *
	 * @return DB object representing a MongoDB connection
	 */
	public MongoClient getClient()
	{
		return client;
	}

	/**
	 * Closes all datasource connections
	 */
	public void close()
	{
		client.close();
	}
}