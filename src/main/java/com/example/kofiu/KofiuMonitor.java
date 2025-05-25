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
            // 1. 부칙 텍스트 추출
            Document doc = Jsoup.connect(TARGET_URL).get();

            // 부칙 항목만 골라서 텍스트 추출 (p 태그 중 '부칙' 포함된 부분)
            Elements updates = doc.select(".law_text .content p:matchesOwn(^부칙<제.*호.*>)");
            String latestVersion = updates.text().trim();

            if (latestVersion.isEmpty()) {
                throw new IllegalStateException("부칙 텍스트를 찾을 수 없습니다.");
            }

            // 2. 이전 저장된 버전 불러오기
            String lastVersion = loadLastVersion().trim();

            // 3. 비교 및 처리
            if (!latestVersion.equals(lastVersion)) {
                System.out.println("🔔 부칙이 업데이트되었습니다: " + latestVersion);
                EmailSender.send("[변경있다] 부칙이 업데이트되었습니다", latestVersion);
                saveLatestVersion(latestVersion);
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
