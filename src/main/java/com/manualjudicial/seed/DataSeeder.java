package com.manualjudicial.seed;

import com.manualjudicial.manual.Manual;
import com.manualjudicial.manual.ManualRepository;
import com.manualjudicial.chapters.Chapter;
import com.manualjudicial.chapters.ChapterRepository;
import com.manualjudicial.questions.Question;
import com.manualjudicial.questions.QuestionRepository;
import com.manualjudicial.questions.QuestionType;
import com.manualjudicial.users.Role;
import com.manualjudicial.users.User;
import com.manualjudicial.users.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

        private final UserRepository userRepository;
        private final ManualRepository manualRepository;
        private final ChapterRepository chapterRepository;
        private final QuestionRepository questionRepository;
        private final PasswordEncoder passwordEncoder;

        @Override
        public void run(String... args) {
                seedAdmin();
                seedData();
        }

        private void seedAdmin() {
                if (userRepository.existsByEmail("admin@manual.cl")) {
                        log.info("Admin user already exists, skipping seed.");
                        return;
                }
                User admin = User.builder()
                                .name("Administrador")
                                .email("admin@manual.cl")
                                .password(passwordEncoder.encode("admin123"))
                                .role(Role.ADMIN)
                                .customThreshold(70)
                                .build();
                userRepository.save(admin);
                log.info("✅ Admin user created: admin@manual.cl / admin123");
        }

        private void seedData() {
                if (manualRepository.count() > 0) {
                        log.info("Data already seeded, skipping content seed.");
                        return;
                }

                // 1. Create Manual
                Manual manual = manualRepository.save(Manual.builder()
                                .title("Derecho Procesal I")
                                .description("Introducción al derecho procesal y orgánica de tribunales.")
                                .build());
                log.info("✅ Manual created: {}", manual.getTitle());

                // 2. Create Chapter
                Chapter chapter1 = chapterRepository.save(Chapter.builder()
                                .manual(manual)
                                .orderIndex(1)
                                .number(1)
                                .title("Sección General")
                                .description("Conceptos fundamentales del ordenamiento jurídico chileno.")
                                .build());
                log.info("✅ Chapter 1 created: Sección General");

                // 3. Create Questions
                questionRepository.save(Question.builder()
                                .chapter(chapter1)
                                .type(QuestionType.COMBINED)
                                .statement("Requisitos constitucionales para ser Presidente de la República. " +
                                                "Determine cuáles de las siguientes afirmaciones son correctas:\n" +
                                                "I. Tener la nacionalidad chilena.\n" +
                                                "II. Tener 35 años de edad cumplidos.\n" +
                                                "III. Tener derecho a sufragio.")
                                .optA("Solo I")
                                .optB("Solo I y II")
                                .optC("Solo II y III")
                                .optD("Solo I y III")
                                .optE("I, II y III")
                                .correctOpt("E")
                                .explanation("Todas son correctas según el artículo 25 de la C.P.R.")
                                .build());

                questionRepository.save(Question.builder()
                                .chapter(chapter1)
                                .type(QuestionType.DIRECT)
                                .statement("¿Qué función cumple la distinción de poderes del Estado?")
                                .optA("Concentrar el poder en una sola autoridad")
                                .optB("Evitar la arbitrariedad y el abuso del poder")
                                .optC("Acelerar la creación de leyes")
                                .optD("Eliminar el Congreso Nacional")
                                .optE("Otorgar poder absoluto al Presidente")
                                .correctOpt("B")
                                .explanation("Garantiza un sistema de pesos y contrapesos.")
                                .build());
                log.info("✅ Demo questions created.");
        }
}
