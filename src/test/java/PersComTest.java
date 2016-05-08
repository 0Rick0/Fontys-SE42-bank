import bank.dao.AccountDAO;
import bank.dao.AccountDAOJPAImpl;
import bank.domain.Account;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import java.sql.SQLException;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertSame;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.*;

/**
 * Created by Rick on 25-4-2016.
 */
public class PersComTest {
    private static EntityManagerFactory emf = Persistence.createEntityManagerFactory("bankPU");

    @BeforeClass
    public static void beforeClass(){
        EntityManager em = emf.createEntityManager();
        util.DatabaseCleaner cleaner = new util.DatabaseCleaner(em);
        try {
            cleaner.clean();
        } catch (SQLException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }
    
    private static long accountnr = 100L;
    
    @Test
    public void test(){
        EntityManager em = emf.createEntityManager();
        Account account = new Account(accountnr++);
        em.getTransaction().begin();
        em.persist(account);
//TODO: verklaar en pas eventueel aan
        assertNull(account.getId());
        em.getTransaction().commit();
        System.out.println("AccountId: " + account.getId());
//TODO: verklaar en pas eventueel aan
        assertTrue(account.getId() > 0L);

        em.getTransaction().begin();
        account.add(100L);
        em.persist(account);
        em.getTransaction().commit();

    }

    @Test
    public void test2(){
        EntityManager em = emf.createEntityManager();
        AccountDAO accountManager = new AccountDAOJPAImpl(em);
        int beginCount = accountManager.count();
        Account account = new Account(accountnr++);
        em.getTransaction().begin();
        em.persist(account);
        assertNull(account.getId());
        em.getTransaction().rollback();
// TODO code om te testen dat table account geen records bevat. Hint: bestudeer/gebruik AccountDAOJPAImpl
        AccountDAOJPAImpl i = new AccountDAOJPAImpl(em);
        assertEquals(beginCount,i.count());
    }

    @Test
    public void test3(){
        EntityManager em = emf.createEntityManager();
        Long expected = -100L;
        Account account = new Account(accountnr++);
        account.setId(expected);
        em.getTransaction().begin();
        em.persist(account);
//TODO: verklaar en pas eventueel aan
        assertEquals(expected, account.getId());
        System.out.println(account.getId());
        em.flush();
//TODO: verklaar en pas eventueel aan
        assertNotEquals(expected, account.getId());
        System.out.println(account.getId());
        em.getTransaction().commit();
//TODO: verklaar en pas eventueel aan

    }

    @Test
    public void test4(){
        EntityManager em = emf.createEntityManager();
        Long expectedBalance = 400L;
        Account account = new Account(accountnr++);
        em.getTransaction().begin();
        em.persist(account);
        account.setBalance(expectedBalance);
        em.getTransaction().commit();
        assertEquals(expectedBalance, account.getBalance());
//TODO: verklaar de waarde van account.getBalance
        Long acId = account.getId();
        account = null;
        EntityManager em2 = emf.createEntityManager();
        em2.getTransaction().begin();
        Account found = em2.find(Account.class, acId);
//TODO: verklaar de waarde van found.getBalance
        assertEquals(expectedBalance, found.getBalance());

    }

    @Test
    public void test5(){
        EntityManager em = emf.createEntityManager();
        EntityManager em2 = emf.createEntityManager();
        Account acc1 = new Account(accountnr++);
        em.getTransaction().begin();
        em.persist(acc1);
        em.getTransaction().commit();

        Account acc2 = em2.find(Account.class, acc1.getId());

        em.getTransaction().begin();
        acc1.add(100L);
        em.getTransaction().commit();

        em2.refresh(acc2);
        assertEquals(acc1.getBalance(),acc2.getBalance());
    }

    @Test
    public void test6(){
        EntityManager em = emf.createEntityManager();
        Account acc = new Account(1L);
        Account acc2 = new Account(2L);
        Account acc9 = new Account(9L);

// scenario 1
        Long balance1 = 100L;
        em.getTransaction().begin();
        em.persist(acc);
        acc.setBalance(balance1);
        em.getTransaction().commit();
//TODO: voeg asserties toe om je verwachte waarde van de attributen te verifieren.
//TODO: doe dit zowel voor de bovenstaande java objecten als voor opnieuw bij de entitymanager opgevraagde objecten met overeenkomstig Id.


// scenario 2
        Long balance2a = 211L;
        acc = new Account(2L);
        em.getTransaction().begin();
        acc9 = em.merge(acc);
        acc.setBalance(balance2a);
        acc9.setBalance(balance2a+balance2a);
        em.getTransaction().commit();
//TODO: voeg asserties toe om je verwachte waarde van de attributen te verifiëren.
//TODO: doe dit zowel voor de bovenstaande java objecten als voor opnieuw bij de entitymanager opgevraagde objecten met overeenkomstig Id.
// HINT: gebruik acccountDAO.findByAccountNr


// scenario 3
        Long balance3b = 322L;
        Long balance3c = 333L;
        acc = new Account(3L);
        em.getTransaction().begin();
        acc2 = em.merge(acc);
        assertFalse(em.contains(acc)); // verklaar, in this entity manager acc is only merged not persisted. In a merge the output object is persisted not the input.
        assertTrue(em.contains(acc2)); // verklaar
        assertNotEquals(acc,acc2);  //verklaar, completely different objects/points to different object. acc2 is persisted.
        acc2.setBalance(balance3b);
        acc.setBalance(balance3c);
        em.getTransaction().commit();
//TODO: voeg asserties toe om je verwachte waarde van de attributen te verifiëren.
//TODO: doe dit zowel voor de bovenstaande java objecten als voor opnieuw bij de entitymanager opgevraagde objecten met overeenkomstig Id.


// scenario 4
        Account account = new Account(114L);
        account.setBalance(450L);
        em = emf.createEntityManager();
        em.getTransaction().begin();
        em.persist(account);
        em.getTransaction().commit();

        Account account2 = new Account(114L);
        Account tweedeAccountObject = account2;
        tweedeAccountObject.setBalance(650L);
        assertEquals((Long)650L,account2.getBalance());  //verklaar
        account2.setId(account.getId());
        em.getTransaction().begin();
        account2 = em.merge(account2);
        assertSame(account,account2);  //verklaar
        assertTrue(em.contains(account2));  //verklaar
        assertFalse(em.contains(tweedeAccountObject));  //verklaar
        tweedeAccountObject.setBalance(850L);
        assertEquals((Long)650L,account.getBalance());  //verklaar
        assertEquals((Long)650L,account2.getBalance());  //verklaar
        em.getTransaction().commit();
        em.close();

    }

    @Test
    public void test7(){
        EntityManager em = emf.createEntityManager();
        Account acc1 = new Account(77L);
        em.getTransaction().begin();
        em.persist(acc1);
        em.getTransaction().commit();
//Database bevat nu een account.

// scenario 1
        Account accF1;
        Account accF2;
        accF1 = em.find(Account.class, acc1.getId());
        accF2 = em.find(Account.class, acc1.getId());
        assertSame(accF1, accF2);

// scenario 2
        accF1 = em.find(Account.class, acc1.getId());
        em.clear();
        accF2 = em.find(Account.class, acc1.getId());
        assertNotSame(accF1, accF2);//The accF1 object is no longer persisted while accF2 is. so the objects are different.
//TODO verklaar verschil tussen beide scenario's

    }

    @Test
    public void test8(){
        EntityManager em = emf.createEntityManager();
        Account acc1 = new Account(88L);
        em.getTransaction().begin();
        em.persist(acc1);
        em.getTransaction().commit();
        Long id = acc1.getId();
//Database bevat nu een account.

        em.remove(acc1);
        assertEquals(id, acc1.getId());
        Account accFound = em.find(Account.class, id);
        assertNull(accFound);
//TODO: verklaar bovenstaande asserts

    }

}
