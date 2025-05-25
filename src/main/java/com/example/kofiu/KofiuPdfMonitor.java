package com.example.kofiu;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;

public class KofiuPdfMonitor {

    private static final String TARGET_URL = "https://www.law.go.kr/행정규칙/금융거래등제한대상자지정및지정취소에관한규정";
    private static final String STORAGE_FILE = "last_version.txt";
    private static final String PDF_FILE = "latest_rule.pdf";

    public static void main(String[] args) {
        try {
            // 1. 부칙 PDF 링크 추출
            Document doc = Jsoup.connect(TARGET_URL).get();
            Element pdfLink = doc.selectFirst("a[href$=.pdf]");

            if (pdfLink == null) {
                throw new IllegalStateException("❗ PDF 링크를 찾을 수 없습니다.");
            }

            String pdfUrl = pdfLink.absUrl("href");
            System.out.println("📎 PDF 링크: " + pdfUrl);

            // 2. PDF 다운로드 (Java 8 방식)
            URL url = new URL(pdfUrl);
            URLConnection connection = url.openConnection();
            InputStream in = connection.getInputStream();
            OutputStream out = new FileOutputStream(PDF_FILE);

            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            in.close();
            out.close();

            // 3. PDF 텍스트 추출
            String pdfText;
            try (PDDocument document = PDDocument.load(new File(PDF_FILE))) {
                PDFTextStripper stripper = new PDFTextStripper();
                pdfText = stripper.getText(document).trim();
            }

            // 4. 이전 내용과 비교
            String lastText = loadLastVersion().trim();

            if (!pdfText.equals(lastText)) {
                System.out.println("🔔 부칙 PDF 내용이 변경되었습니다.");
                EmailSender.send("[변경있다] 부칙 PDF가 변경되었습니다", pdfText.substring(0, Math.min(500, pdfText.length())));
                saveLatestVersion(pdfText);
            } else {
                System.out.println("부칙 변경 없음.");
                EmailSender.send("[변경없다] 부칙 PDF 변경 없음", "금일 부칙에 변경 사항이 없습니다.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            EmailSender.send("[오류 발생] 부칙 PDF 확인 중 오류", e.toString());
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
