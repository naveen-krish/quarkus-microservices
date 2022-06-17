package lab.quarkus.microservices.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import lab.quarkus.microservices.entity.Compensation;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CompensationRepository implements PanacheRepository<Compensation> {

    public void saveCompensation(Compensation compensation){
        persist(compensation);
    }

    public Compensation fetchCompensation(String lraIdUrl){
        return find("lraIdUrl",lraIdUrl).firstResult();
    }
}
