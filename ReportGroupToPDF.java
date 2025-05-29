package ua.oleksa.coins;




import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.font.PdfFontFactory.EmbeddingStrategy;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.*;

import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;

import org.apache.commons.imaging.ImageFormats;
import org.apache.commons.imaging.Imaging;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.*;


import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.*;

import java.util.*;
import java.util.List;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;


public class ReportGroupToPDF {


    static class CoinData {
        String title;
        String value;
        String series;
        String issuedate;
        String unit;
        String material;
        String id;
        byte[] obv;
        byte[] rev;

        public CoinData(String title, String value, String series, String issuedate, String unit, String material, byte[] obv, byte[] rev, String id) {
            this.title = title;
            this.value = value;
            this.series = series;
            this.issuedate = issuedate;
            this.unit = unit;
            this.material = material;
            this.obv = obv;
            this.rev = rev;
            this.id = id;
        }
    }

    private static final String[] TRY_FORMATS1 = {"png", "jpeg", "jpg", "bmp", "gif", "tiff","webp"};
    private static final ImageFormats[] TRY_FORMATS2 = {
            ImageFormats.PNG,
            ImageFormats.JPEG,
            ImageFormats.TIFF,
            ImageFormats.GIF,
            ImageFormats.BMP,
            ImageFormats.PNM,
            ImageFormats.WEBP
    };


    public static byte[] tryDecodeImage(byte[] imageBytes) {
        for (String format : TRY_FORMATS1) {
            try {
                ByteArrayInputStream bais = new ByteArrayInputStream(imageBytes);
                BufferedImage img = ImageIO.read(bais);

                if (img != null) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ImageIO.write(img, format, baos);
                    return baos.toByteArray();
                }
            } catch (IOException e) {
                // ігноруємо й пробуємо наступний формат
            }
        }
        System.out.println("Image format incorrect.");
        return null;
    }


    public static byte[] fixImageWithCommonsImaging(byte[] imageBytes) {
            for (ImageFormats format : TRY_FORMATS2) {
                try {
                    BufferedImage image = Imaging.getBufferedImage(imageBytes);
                    if (image != null) {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        Imaging.writeImage(image, baos, format);
                        return baos.toByteArray();
                    }
                }
                catch (IOException e) {
                    // ігноруємо й пробуємо наступний формат
                }

            }
            System.out.println("Image format incorrect.");
            return null;
    }



    public static byte[] fixImageWithTwelveMonkeys(byte[] imageBytes) {
            for (String format : TRY_FORMATS1) {
                try {
                    ByteArrayInputStream bais = new ByteArrayInputStream(imageBytes);
                    BufferedImage image = ImageIO.read(bais);
                    if (image != null) {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        ImageIO.write(image, format, baos);
                        return baos.toByteArray();
                    }
                }
                catch (IOException e) {
                    // ігноруємо й пробуємо наступний формат
                }
            }

            System.out.println("Image format incorrect.");
            return null;
    }










    private static Connection connect() {
        String url = "jdbc:sqlite:F:\\temp\\OpenNumismat\\my_coins2.db";
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url);
        } catch (SQLException e) {
            System.out.println("Connection error: " + e.getMessage());
        }
        return conn;
    }


    private static void addTableHeader(Table table, PdfFont boldFont) {
        String[] headers = {"Зображення", "Назва", "Номінал", "Дата випуску", "Матеріал"};
       /*
        doc.add(new Paragraph("Серія: " + ser)
                .setFont(boldFont).setFontSize(16)
                .setMarginTop(10).setMarginBottom(5));*/

        for (String header : headers) {
            table.addHeaderCell(
                    new Cell()
                            .add(new Paragraph(header)
                                    .setFont(boldFont)
                                    .setFontColor(ColorConstants.DARK_GRAY))
                            .setBackgroundColor(MyColors.MYBLUE)
                            .setTextAlignment(TextAlignment.CENTER)
                            .setPadding(5)
            );
        }
    }

    private static String safeText(String str) {
        return (str != null) ? str : "";
    }

    public static void main(String[] args) {

        //  CoinDatabase db = new CoinDatabase();

        String outputPdf = "report_v8.pdf";
        int imgSize = 120;

        try {

            PdfWriter writer = new PdfWriter(new FileOutputStream(outputPdf));
            PdfDocument pdfDoc = new PdfDocument(writer);

            Document document = new Document(pdfDoc, PageSize.A4/*.rotate()*/);
            document.setMargins(30, 20, 30, 20);


            // PdfFont font = PdfFontFactory.createFont(StandardFonts.HELVETICA);

            PdfFont font = PdfFontFactory.createFont("C:/Windows/Fonts/times.ttf", PdfEncodings.IDENTITY_H, EmbeddingStrategy.PREFER_EMBEDDED);
            //PdfFont boldFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            PdfFont boldFont = PdfFontFactory.createFont("C:/Windows/Fonts/timesbd.ttf", PdfEncodings.IDENTITY_H, EmbeddingStrategy.PREFER_EMBEDDED);

            // Створення таблиці з 4 колонками
            //  Table table = new Table(new PercentColumnWidth(new float[]{2, 3, 3, 4}));
            // Залишає таблицю цілісною при переході на нову сторінку


            // Заголовки таблиці
            // addTableHeader(table, boldFont);


            Map<String, List<CoinData>> groupedData = new LinkedHashMap<>();

            try (Connection conn = connect();
                 PreparedStatement ps = conn.prepareStatement("select c.id, c.obverseimg, c.reverseimg, c.title, c.value, c.issuedate, c.series, c.unit, c.material, p1.image as obv, p2.image as rev " +
                         "from coins c " +
                         "left join photos p1 on c.obverseimg=p1.id " +
                         "left join photos p2 on c.reverseimg=p2.id " +
                         "where  c.status = 'owned' and (c.id=986 or c.id=939) "); //c.id <=100 and   and c.title='Рік Змії'
                 ResultSet rs = ps.executeQuery()) {


                while (rs.next()) {

                    String series = rs.getString("series");
                    CoinData coin = new CoinData(
                            rs.getString("title"),
                            rs.getString("value"),
                            series,
                            rs.getString("issuedate"),
                            rs.getString("unit"),
                            rs.getString("material"),
                            rs.getBytes("obv"),
                            rs.getBytes("rev"),
                            rs.getString("id")
                    );
                    groupedData.computeIfAbsent(series, k -> new ArrayList<>()).add(coin);

                }

            } catch (SQLException e) {
                {
                    System.out.println("Err DB: " + e.getMessage());
                }
            }

            for (Map.Entry<String, List<CoinData>> entry : groupedData.entrySet()) {

                String series = entry.getKey();
                List<CoinData> coins = entry.getValue();

                document.add(new Paragraph("Серія: " + series)
                        .setFont(boldFont).setFontSize(14)
                        .setMarginTop(10).setMarginBottom(5));

                float[] columnWidths = {52, 12, 12, 12, 12}; // Відсоткові значення для кожної колонки
                Table table = new Table(UnitValue.createPercentArray(columnWidths));
                table.setWidth(UnitValue.createPercentValue(100));
                table.setFont(font);
                table.setFontSize(10);
                table.setKeepTogether(true);


                addTableHeader(table, boldFont);

                for (CoinData coin : coins) {


                    byte[] imageBytesObvD = coin.obv;
                    byte[] imageBytesObv = fixImageWithCommonsImaging(imageBytesObvD);

                    byte[] imageBytesRevD = coin.rev;
                    byte[] imageBytesRev = fixImageWithCommonsImaging(imageBytesRevD);

                    Table imageTable = new Table(2);
                    imageTable.setWidth(UnitValue.createPercentValue(100));

                    if (imageBytesObv != null && imageBytesObv.length > 0) {
                        Image imgObv = new Image(ImageDataFactory.create(imageBytesObv)).scaleToFit(imgSize, imgSize);
                        imageTable.addCell(new Cell().add(imgObv).setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.CENTER));
                    }

                    if (imageBytesRev != null && imageBytesRev.length > 0) {
                        Image imgRev = new Image(ImageDataFactory.create(imageBytesRev)).scaleToFit(imgSize, imgSize);
                        imageTable.addCell(new Cell().add(imgRev).setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.CENTER));

                    }

                    Cell imgCell = new Cell()
                            .add(imageTable)
                            .setTextAlignment(TextAlignment.CENTER)
                            .setVerticalAlignment(VerticalAlignment.MIDDLE);

                    table.addCell(imgCell);

                    table.addCell(new Cell().add(new Paragraph(safeText(coin.title)))
                                    .setTextAlignment(TextAlignment.LEFT))
                            .setVerticalAlignment(VerticalAlignment.MIDDLE);
                    System.out.println(coin.title);

                    table.addCell(new Cell().add(new Paragraph(safeText(coin.value + " " + coin.unit)))
                                    .setTextAlignment(TextAlignment.CENTER))
                            .setVerticalAlignment(VerticalAlignment.MIDDLE);
                    System.out.println(coin.id);
/*
                    table.addCell(new Cell().add(new Paragraph(safeText(coin.series)))
                                    .setTextAlignment(TextAlignment.CENTER))
                            .setVerticalAlignment(VerticalAlignment.MIDDLE);
                    System.out.println(coin.series);*/

                    table.addCell(new Cell().add(new Paragraph(safeText(coin.issuedate)))
                                    .setTextAlignment(TextAlignment.CENTER))
                            .setVerticalAlignment(VerticalAlignment.MIDDLE);
                    System.out.println(coin.issuedate);

                    table.addCell(new Cell().add(new Paragraph(safeText(coin.material)))
                                    .setTextAlignment(TextAlignment.CENTER))
                            .setVerticalAlignment(VerticalAlignment.MIDDLE);
                    System.out.println(coin.material);

                }

                document.add(table);
                document.add(new AreaBreak(AreaBreakType.NEXT_PAGE));

            }


            document.close();

            System.out.println("PDF згенеровано: " + outputPdf);


        } catch (IOException e) {
            System.out.println("Err IO: " + e.getMessage());
        }


    }

}

