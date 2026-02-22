package com.example.app.ejb;

import com.example.app.entity.HitResult;
import com.example.app.entity.User;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.List;

@Stateless
public class HitServiceBean {
    @PersistenceContext(unitName = "primary")
    private EntityManager em;

    public boolean checkHit(double x, double y, double r) {
        // Пример для 3-й четверти + треугольник (вариант 12345)
        boolean inCircle = (x <= 0 && y <= 0 && Math.pow(x, 2) + Math.pow(y, 2) <= Math.pow(r, 2));
        boolean inTriangle = (x >= 0 && y <= 0 && x <= r/2 && y >= (2*x - r));
        return inCircle || inTriangle;
    }

    public HitResult saveResult(double x, double y, double r, boolean hit, User user) {
        HitResult result = new HitResult();
        result.setX(x);
        result.setY(y);
        result.setR(r);
        result.setHit(hit);
        result.setTimestamp(LocalDateTime.now());
        result.setUser(user);
        em.persist(result);
        return result;
    }

    public List<HitResult> getUserHistory(User user) {
        return em.createQuery(
                        "SELECT h FROM HitResult h WHERE h.user = :user ORDER BY h.timestamp DESC",
                        HitResult.class
                )
                .setParameter("user", user)
                .setMaxResults(10)
                .getResultList();
    }
}