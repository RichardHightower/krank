package com.arcmind.jpa.course.simple.model;


import java.io.File;
import java.io.FilenameFilter;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import org.hibernate.LazyInitializationException;


import junit.framework.TestCase;

public class RelationshipsTest extends TestCase {

	private EntityManager entityManager;
	private EntityManagerFactory entityManagerFactory;
	private EntityTransaction transaction;
	
	private String[] roles = new String[]{"ADMIN", "USER", "SUPER_USER"};
	
	private void crreateRoles() throws Exception {
		/* Setup the roles. */
		execute(new TransactionTemplate(){
			public Object execute() {
				
				for (String sRole : roles) {
					entityManager.persist(new Role(sRole));
				}
				return null;
			}
		});
	}

	static {
		//destroyDB();
	}

	protected void setUp() throws Exception {
		/* Use Persistence.createEntityManagerFactory to create 
		 * "security-domain" persistence unit. */ 
		entityManagerFactory = Persistence.createEntityManagerFactory("security-domain");

		deleteRoles();
		crreateRoles();
		
	}


	private static void destroyDB() {
		File tmpDir = new File("/TMP");
		if (tmpDir.exists()) {
			File[] files = tmpDir.listFiles(new FilenameFilter() {

				public boolean accept(File dir, String name) {
					return name.contains("PROTO");
				}
				
			});
			for (File file : files) {
				file.delete();
			}
		}
		
	}


	

	protected void tearDown() throws Exception {

		if(entityManager!=null && entityManager.isOpen()) {
			entityManager.close();
		}
		if (transaction!=null && transaction.isActive()) {
			if (transaction.getRollbackOnly()) {
				transaction.rollback();
			}
		}

		try {
			deleteRoles();
		} catch (Exception ex) {
			destroyDB();
		}
		
		entityManager = null;
		transaction = null;
	}


	private void deleteRoles() throws Exception {
		/* Delete the roles. */
		execute(new TransactionTemplate(){
			public Object execute() {
				
				entityManager.createQuery("delete Role").executeUpdate();
				
				return null;
			}
		});
	}
	
	public void testGroupUserRelationship() throws Exception {

		/* Construct the group. */
		final Group group = new Group("sysadmins");
		group.getUsers().add(new User("RickHigh"));
		group.getUsers().add(new User("PaulHix"));
		group.getUsers().add(new User("PaulTab", 
				new ContactInfo("5205551212", "Paul", "Taboraz", 
				new Address("123 Main", "", "85748", "AZ"))));

		/* Persist the group. */
		execute(new TransactionTemplate(){
			
			public Object execute() {
				
				entityManager.persist(group);
				
				/* Associate the group with a role. */
				group.getRoles().add(
						(Role)
						entityManager.createNamedQuery("loadRole")
							.setParameter("name", "ADMIN")
							.getSingleResult());
				
				/* Write the users associated with this group. */
				for (User user : group.getUsers()) {
					entityManager.persist(user);
					if (user.getContactInfo() != null) {
						entityManager.persist(user.getContactInfo());
					}
				}
				
				return null;
			}
			
		});
		
		/* Shut down the entityManager session. */
		entityManager.close();
		entityManager = entityManagerFactory.createEntityManager();

		/* Read the group. */
		Group loadedGroup = (Group) execute(new TransactionTemplate(){
			
			public Object execute() {
				
				return (Group) 
				entityManager.createNamedQuery("loadGroup")
				.setParameter("name", "sysadmins").getSingleResult();
				
			}
			
		});
		
		/* Ensure it was written to the database correctly. */
		assertEquals("sysadmins", loadedGroup.getName()); //1
		assertEquals("ADMIN", loadedGroup.getRoles().get(0).getName()); //2
		assertEquals("PaulTab", loadedGroup.getUsers().get(2).getName()); //3
		assertEquals("5205551212", loadedGroup.getUsers().get(2)
				.getContactInfo().getPhone()); //4
		assertEquals("85748", loadedGroup.getUsers().get(2)
				.getContactInfo().getAddress().getZip()); //5


		/* Demonstrate laziness issues. ----------------------------------------------- */
		entityManager.close();
		entityManager = entityManagerFactory.createEntityManager();

		/* Reread the group. */
		loadedGroup = (Group) execute(new TransactionTemplate(){
			
			public Object execute() {
				
				return (Group) 
				entityManager.createNamedQuery("loadGroup")
				.setParameter("name", "sysadmins").getSingleResult();
				
			}
			
		});

		entityManager.close();
		
		try {
		   assertEquals("ADMIN", loadedGroup.getRoles().get(0).getName()); //1
		   fail();
		} catch (LazyInitializationException lie) {
			assertTrue(true);
		}
		
		entityManager = entityManagerFactory.createEntityManager();

		/* Reread the group. */
		final Group groupToDelete = (Group) execute(new TransactionTemplate(){		
			public Object execute() {
				return (Group) 
				entityManager.createNamedQuery("loadGroup")
				.setParameter("name", "sysadmins").getSingleResult();
			}
		});
		
		/* Delete the group and all users in the group. */
		// TODO: Utilize IoC to delete the group / users
		//execute(
				/*add logic here, use lecture slides as a guide */
		//);
		
	}

	// TODO: Define a TransactionTemplate interface with a no-argument execute method returning an Object
	public interface TransactionTemplate {
		Object execute();
	}
	
	
	// TODO: Create a local helper method to encapsulate transaction handling (use lecture slides as a guide)
	private Object execute(TransactionTemplate tt) throws Exception {
		// add transaction management and resource cleanup here
		return null; // << remove
	}
	
}