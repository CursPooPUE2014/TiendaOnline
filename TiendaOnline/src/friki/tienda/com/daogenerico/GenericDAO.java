package friki.tienda.com.daogenerico;
/*
 * Clase de implementaci�n para la interfaz IGenericDAO.
 * Extiende la clase de Spring JpaDaoSupport, la cual 
 * proporciona m�todos de conveniencia que utilizaremos como
 * implementaciones por defecto de los m�todos CRUD. 
 * 
 * El par�metro gen�rico T debe extendender a IPersistent usando 
 * el tipo K, lo cual nos permite acceder al m�todo getKey() que 
 * emplearemos para recuperar la clave primaria de cualquier POJO JPA.
 * 
 * No tenemos que preocuparnos sobre el cast (que es obligatorio), 
 * dado que estamos usando el tipo gen�rico del par�metro.
 */


import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.PersistenceException;
import javax.persistence.TypedQuery;

import friki.tienda.com.Persistencia.ConnectionHelper;

/*
import org.springframework.orm.jpa.support.JpaDaoSupport;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;
*/
/*
 * Indicamos a nivel de clase que todos los m�todos soportar�n
 * transacciones y ser�n de s�lo lectura 
 */

public class GenericDAO<K,T extends IPersistent<K>> implements IGenericDAO<K,T> {		
	
	//private Class<T> claseDePersistencia;
	
	EntityManager manager;
	
	private void load(){	
		manager = ConnectionHelper.getInstance().getEntityManager();		
	}
	
	private void close(){
		
		//manager.close();
		
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<T> listAll(final Class<T> clase) {
		String query="SELECT o FROM " + clase.getSimpleName() + " o";
		load();
		
		List<T> listaDeObjetos = null;
		try {		
			TypedQuery<T> consulta = manager.createQuery(query, clase);
			listaDeObjetos = consulta.getResultList();
			
		} finally {
			close();
		}		
		return listaDeObjetos;
	}

	@SuppressWarnings("unchecked")
	public T findByKey(T object, Class<K> tipo) { 
		
		load();
		
		T objeto = null;
		try {
			
			objeto = (T) manager.find((Class<T>)object.getClass(), (K) object.getKey());
			
		} finally {
			close();
		}
		
		return objeto;
	}

	
	public T save(T object) {
		load();
		
		EntityTransaction tx = null;
		try {

			tx = manager.getTransaction();
			tx.begin();
			manager.persist(object);
			tx.commit();

		} catch (PersistenceException e) {
			tx.rollback();
			throw e;
		} catch (Exception e) {
			System.out.println(e.getMessage());
			// retornamos nulo para poder tratar este
			// caso desde el controlador que haga la petici�n
			return null;
			
		} finally {
			close();
			
		}
		return object;
	}

	
	public T update(T object) {
		load();
		
		EntityTransaction tx = null;
		try {

			tx = manager.getTransaction();
			tx.begin();
			manager.merge(object);
			tx.commit();

		} catch (PersistenceException e) {
			tx.rollback();
			throw e;
		} finally {

			close();
		}
		
		return object;
	}
	
	/*
	 * Propagation.REQUIRED indica que el m�todo actual debe 
	 * ejecutarse dentro de una transacci�n. Si hay una 
	 * transacci�n en progreso, el m�todo se ejecutar� dentro
	 * de dicha transacci�n. Si no, se iniciar� una nueva
	 */
	
	public boolean delete(T object) {
		load();
		
		EntityTransaction tx = null;
		try {

			tx = manager.getTransaction();
			tx.begin();
			manager.remove(manager.merge(object));
			tx.commit();
			return true;
		} catch (PersistenceException e) {

			tx.rollback();
			throw e;
			
		} finally {

			close();
		}	
	}
 	
}