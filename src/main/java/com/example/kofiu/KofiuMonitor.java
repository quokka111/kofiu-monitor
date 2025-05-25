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
            // 1. 웹 페이지에서 부칙 정보 추출
            Document doc = Jsoup.connect(TARGET_URL).get();

            // 부칙 제목("부      칙") + 부칙 번호/날짜("제2024-12호, 2024.12.30.")
            Elements bls = doc.select("span.bl");
            Elements sfons = doc.select("span.sfon");

            String latestVersion = "";
            if (!bls.isEmpty() && !sfons.isEmpty()) {
                // 공백 제거 후 한 줄로 조합
                String buChik = bls.get(0).text().replaceAll("\\s+", ""); // "부칙"
                String dateInfo = sfons.get(0).text().trim();             // "제2024-12호, 2024.12.30."
                latestVersion = buChik + " " + dateInfo;
            } else {
                throw new IllegalStateException("부칙 관련 요소를 찾을 수 없습니다. selector를 확인하세요.");
            }

            // 2. 저장된 이전 버전 불러오기
            String lastVersion = loadLastVersion().trim();

            // 3. 비교 및 알림
            if (!latestVersion.equals(lastVersion)) {
                System.out.println("🔔 부칙이 업데이트되었습니다: " + latestVersion);
                EmailSender.send("[변경있다] 부칙이 업데이트되었습니다", latestVersion);
                saveLatestVersion(latestVersion); // 새로 저장
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
