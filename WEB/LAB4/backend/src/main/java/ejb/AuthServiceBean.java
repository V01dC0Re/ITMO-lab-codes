package ejb;

import entity.User;
import org.mindrot.jbcrypt.BCrypt;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

@Stateless
public class AuthServiceBean {
    @PersistenceContext(unitName = "primary")
    private EntityManager em;

    public User authenticate(String login, String password) {
        TypedQuery<User> query = em.createQuery(
                "SELECT u FROM User u WHERE u.login = :login", User.class);
        query.setParameter("login", login);

        try {
            User user = query.getSingleResult();
            if (BCrypt.checkpw(password, user.getPasswordHash())) {
                return user;
            }
        } catch (Exception e) {
            // Логирование ошибки
        }
        return null;
    }

    public void registerDefaultUser() {
        if (em.createQuery("SELECT COUNT(u) FROM User u", Long.class).getSingleResult() == 0) {
            User user = new User();
            user.setLogin("student");
            user.setPasswordHash(BCrypt.hashpw("password", BCrypt.gensalt(12)));
            user.setFullName("Иван Иванов");
            user.setGroupName("P3212");
            user.setVariantNumber("12345");
            em.persist(user);
        }
    }
}