package com.moedamas.busdriver.modules.db;

/**
 * This module provides busDriverServices with a Hibernate session
 * Each request starts a transaction and is given a session.
 * is the request succedes, the transaction is commited. if an exception is thrown, the transaction is rolled back.
 * the hibernate session is made available to the services though the busDriverRequest, under a key with the same name as the module ref.
 * 
 */

import javax.servlet.ServletContext;

import com.moedamas.busdriver.busDriverRequest;
import com.moedamas.busdriver.busException;
import com.moedamas.busdriver.core.ConfigMgr;
import com.moedamas.busdriver.modules.busAbstractModule;

import org.hibernate.*;
import org.hibernate.cfg.*;


public class busHibernateFactory extends busAbstractModule {

	private SessionFactory sessionFactory;
	private String moduleRefTransaction;
	private javax.servlet.ServletContext context;

	public busHibernateFactory(String moduleRef, ConfigMgr config) {
		super(moduleRef, config);
		
		moduleRefTransaction = moduleRef + "_transaction";

	}

	@Override
	public void prepareContext(ServletContext context) throws busException {
		this.context = context;

		try {
			context.log("busHibernateFactory init()... ");

//			Create the SessionFactory 
			sessionFactory = new Configuration().configure().buildSessionFactory(); 
			
//		 	Look up our data source
			context.log("busHibernateFactory Done! ");
		}
		catch (Throwable e) {
			context.log("Initial SessionFactory creation failed.", e); 
			throw new busException(e.getMessage(), e);
		}
		
	}


	@Override
	public void prepareService(busDriverRequest request) throws busException {
			
			Session session = sessionFactory.openSession();
			Transaction transaction = session.beginTransaction();
			request.setValue(moduleRef, session);
			request.setValue(moduleRefTransaction, transaction);

	}

	@Override
	public void finishService(busDriverRequest request, boolean success) throws busException {

		Session session = (Session) request.getValue(moduleRef);
		Transaction transaction = (Transaction) request.getValue(moduleRefTransaction);
		request.removeValue(moduleRef);
		if(success)
			transaction.commit();
		else
			transaction.rollback();
		session.close();
	}

	@Override
	public void finishContext() throws busException {
		// TODO Auto-generated method stub
		
	}


}
