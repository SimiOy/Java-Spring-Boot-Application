package server.database;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class
DeleteDatabaseEntries {

    /**
     * wipes all databases that don't need to be persistent
     * @param qRepository
     * @param pRepository
     * @param lRepository
     * @return
     */
    @Bean
    ApplicationRunner wipeDatabases(QuestionRepository qRepository, PlayerRepository pRepository,
                                    LobbyRepository lRepository){
        return new ApplicationRunner() {
            @Override
            public void run(ApplicationArguments args) throws Exception {
                qRepository.deleteAll();
                pRepository.deleteAll();
                lRepository.deleteAll();
            }
        };
    }
}
