package com.example.kofiu;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import java.io.*;

public class KofiuMonitor {

    private static final String TARGET_URL = "https://www.law.go.kr/행정규칙/금융거래등제한대상자지정및지정취소에관한규정";
    private static final String STORAGE_FILE = "last_version.txt";

    public static void main(String[] args) {
        try {
            // 1. 웹 페이지에서 부칙 텍스트 추출
            Document doc = Jsoup.connect(TARGET_URL).get();

            // 공백 포함 부칙 <제XXXX-XX호, YYYY.MM.DD.> 형식 추출
            Elements updates = doc.select(".law_text .content p:matchesOwn(부\\s*칙\\s*<제\\d{4}-\\d{2}호,\\s*\\d{4}\\.\\d{2}\\.\\d{2}\\s*>)");

            String latestVersion = updates.isEmpty() ? "" : updates.first().text().trim();

            // 부칙이 없으면 오류 처리
            if (latestVersion.isEmpty()) {
                throw new IllegalStateException("부칙 텍스트를 찾을 수 없습니다. 페이지 형식을 점검하세요.");
            }

            // 2. 이전 저장된 버전 불러오기
            String lastVersion = loadLastVersion().trim();

            // 3. 변경 비교
            if (!latestVersion.equals(lastVersion)) {
                System.out.println("🔔 부칙이 업데이트되었습니다: " + latestVersion);
                EmailSender.send("[변경있다] 부칙이 업데이트되었습니다", latestVersion);
                saveLatestVersion(latestVersion);  // 파일에 저장
            } else {
                System.out.println("부칙 변경 없음.");
                EmailSender.send("[변경없다] 부칙 변경 없음", "금일 부칙에 변경 사항이 없습니다.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            EmailSender.send("[오류 발생] 부칙 확인 중 오류", e.toString());
        }
    }

    private static String loadLastVersion() {
        File file = new File(STORAGE_FILE);
        if (!file.exists()) return "";
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            return br.readLine();
        } catch (IOException e) {
            return "";
        }
    }

    private static void saveLatestVersion(String version) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(STORAGE_FILE))) {
            bw.write(version);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
