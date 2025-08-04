package com.umc.hwaroak.util;

import net.coobird.thumbnailator.Thumbnails;

import java.awt.image.BufferedImage;
import java.io.*;

import javax.imageio.ImageIO;

public class ImageFormatter {

    public static ByteArrayInputStream convertToWebP(InputStream inputStream, int width, int height) {
        try {
            BufferedImage originalImage = ImageIO.read(inputStream);

            ByteArrayOutputStream os = new ByteArrayOutputStream();
            Thumbnails.of(originalImage)
                    .size(width, height)
                    .outputFormat("jpg") // WebP로 바꾸려면 라이브러리 추가 필요
                    .toOutputStream(os);

            return new ByteArrayInputStream(os.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("이미지 포맷 변환 실패", e);
        }
    }
}
