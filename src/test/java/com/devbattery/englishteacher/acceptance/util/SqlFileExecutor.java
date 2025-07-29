package com.devbattery.englishteacher.acceptance.util;

import org.springframework.stereotype.Service   ;

@Service
public class SqlFileExecutor {

//    @PersistenceContext
//    private EntityManager entityManager;
//
//    @Transactional
//    public void execute(String sqlFilePath) {
//        try {
//            entityManager.flush();
//            String path = sqlFilePath;
//            String sqlScript = readResourceFile(path);
//
//            String[] queries = sqlScript.split(";");
//            for (String query : queries) {
//                if (!query.trim().isEmpty()) {
//                    entityManager.createNativeQuery(query).executeUpdate();
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    private String readResourceFile(String path) throws IOException {
//        Resource resource = new ClassPathResource(path);
//        try (InputStream inputStream = resource.getInputStream();
//             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
//            return reader.lines().collect(StringBuilder::new, StringBuilder::append, StringBuilder::append).toString();
//        }
//    }

}
