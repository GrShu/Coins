package ua.oleksa.coins;

import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.*;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.*;
import com.itextpdf.kernel.font.PdfFontFactory.EmbeddingStrategy;
import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.layout.borders.Border;

import java.io.*;
import java.sql.*;


public class ReportToPDF {

    private static Connection connect() {
        String url = "jdbc:sqlite:D:\\Aleks\\OpenNumismat\\my_coins.db";
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url);
        } catch (SQLException e) {
            System.out.println("Connection error: " + e.getMessage());
        }
        return conn;
    }


    private static void addTableHeader(Table table, PdfFont boldFont) {
        String[] headers = {"Зображення", "Назва", "Номінал", "Серія", "Дата випуску", "Матеріал"};
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
        float[] columnWidths = {5, 1, 1, 1, 1, 1}; // Відсоткові значення для кожної колонки
        Table table = new Table(UnitValue.createPercentArray(columnWidths));
        table.setWidth(UnitValue.createPercentValue(100));
        table.setFont(font);
        table.setFontSize(10);
        table.setKeepTogether(true); // Залишає таблицю цілісною при переході на нову сторінку


        // Заголовки таблиці
        addTableHeader(table, boldFont);

        try (Connection conn = connect();
             PreparedStatement ps = conn.prepareStatement("select c.title, c.value, c.issuedate, c.series, c.unit, c.material, p1.image as obv, p2.image as rev " +
                     "from coins c " +
                     "left join photos p1 on c.obverseimg=p1.id " +
                     "left join photos p2 on c.reverseimg=p2.id " +
                     "where c.id <=100 and c.status = 'owned'");
             ResultSet rs = ps.executeQuery()) {

            int rowCounter = 0;
            while (rs.next()) {
                // Приклад: перша колонка буде об’єднана на 3 рядки



                byte[] imageBytesObv = rs.getBytes("obv");
                byte[] imageBytesRev = rs.getBytes("rev");

                Table imageTable = new Table(2);
                imageTable.setWidth(UnitValue.createPercentValue(100)); // або задайте конкретну ширину

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

                    /*
                    if (rowCounter == 0) {
                        Cell mergedCell = new Cell(3, 1)
                                .add(img)
                                .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                                .setTextAlignment(TextAlignment.CENTER);
                        table.addCell(mergedCell);
                    }*/



                    //table.addCell(new Cell().add(img).setTextAlignment(TextAlignment.CENTER));
                /*
                } else {
                    table.addCell(new Cell().add(new Paragraph("No image")).setTextAlignment(TextAlignment.CENTER));
                }*/




                table.addCell(new Cell().add(new Paragraph(safeText(rs.getString("title"))))
                        .setTextAlignment(TextAlignment.LEFT))
                        .setVerticalAlignment(VerticalAlignment.MIDDLE);
                System.out.println(rs.getString("title"));

                table.addCell(new Cell().add(new Paragraph(safeText(rs.getString("value") + " " + rs.getString("unit"))))
                        .setTextAlignment(TextAlignment.CENTER))
                        .setVerticalAlignment(VerticalAlignment.MIDDLE);
                System.out.println(rs.getString("value"));

                table.addCell(new Cell().add(new Paragraph(safeText(rs.getString("series"))))
                                .setTextAlignment(TextAlignment.CENTER))
                        .setVerticalAlignment(VerticalAlignment.MIDDLE);
                System.out.println(rs.getString("series"));

                table.addCell(new Cell().add(new Paragraph(safeText(rs.getString("issuedate"))))
                        .setTextAlignment(TextAlignment.CENTER))
                        .setVerticalAlignment(VerticalAlignment.MIDDLE);
                System.out.println(rs.getString("issuedate"));

                table.addCell(new Cell().add(new Paragraph(safeText(rs.getString("material"))))
                                .setTextAlignment(TextAlignment.CENTER))
                        .setVerticalAlignment(VerticalAlignment.MIDDLE);
                System.out.println(rs.getString("material"));

                rowCounter++;


            }


        }
        catch (SQLException e) {
            {
                System.out.println("Err DB: " + e.getMessage());
            }
        }

        document.add(table);
        document.close();

        System.out.println("PDF згенеровано: " + outputPdf);



    }catch (IOException e) {
        System.out.println("Err IO: " + e.getMessage());
    }


    }

}


