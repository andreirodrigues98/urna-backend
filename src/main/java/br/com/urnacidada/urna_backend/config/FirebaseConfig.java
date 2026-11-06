package br.com.urnacidada.urna_backend.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

/** As credenciais ficam fora do projeto e são lidas de GOOGLE_APPLICATION_CREDENTIALS. */
@Configuration
public class FirebaseConfig {
    @Bean
    public Firestore firestore(@Value("${urna.firebase.project-id:}") String projectId) throws IOException {
        FirebaseApp app;
        if (FirebaseApp.getApps().isEmpty()) {
            FirebaseOptions.Builder builder = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.getApplicationDefault());
            if (projectId != null && !projectId.isBlank()) {
                builder.setProjectId(projectId);
            }
            FirebaseOptions options = builder.build();
            app = FirebaseApp.initializeApp(options);
        } else {
            app = FirebaseApp.getInstance();
        }
        return FirestoreClient.getFirestore(app);
    }
}
