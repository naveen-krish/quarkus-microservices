package lab.quarkus.microservices.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import lab.quarkus.microservices.entity.PayLoad;

import javax.enterprise.context.ApplicationScoped;


@ApplicationScoped
public class PayLoadRepository implements PanacheRepository<PayLoad> {
    public long findByHash(int hash) {

        return count("payloadHash = ?1 ",hash);
    }

    public  long countByName(){
        return count();
    }
}
