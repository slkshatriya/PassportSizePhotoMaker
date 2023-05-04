package dev.suraj.PassportSizePhotoMaker;

import org.apache.commons.imaging.ImageFormats;
import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.ImageWriteException;
import org.apache.commons.imaging.Imaging;
import org.imgscalr.Scalr;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Base64;

@Controller
public class PhotoController {

    @GetMapping("/upload")
    public String showUploadForm() {
        return "upload";
    }

    @PostMapping("/upload")
    public ModelAndView handleFileUpload(@RequestParam("file") MultipartFile file) throws IOException, ImageReadException, ImageWriteException {
        byte[] bytes = file.getBytes();

        BufferedImage originalImage = Imaging.getBufferedImage(bytes);

        int width = 600;
        int height = 600;

        BufferedImage resizedImage = Scalr.resize(originalImage, Scalr.Method.ULTRA_QUALITY, Scalr.Mode.AUTOMATIC, width, height, Scalr.OP_ANTIALIAS);

        File output = new File("passport-photo.png");

        OutputStream out = new FileOutputStream(output);
        Imaging.writeImage(resizedImage, out, ImageFormats.PNG, null);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(resizedImage, "png", baos);
        byte[] resizedBytes = baos.toByteArray();
        String resizedImageString = "data:image/png;base64," + Base64.getEncoder().encodeToString(resizedBytes);

        ModelAndView mav = new ModelAndView("result");
        mav.addObject("photoUrl", resizedImageString);
        mav.addObject("photoName", file.getOriginalFilename());
        return mav;
    }


    @GetMapping("/download")
    public void downloadPhoto(HttpServletResponse response, @RequestParam("photoUrl") String photoUrl) throws IOException {
        byte[] bytes = Base64.getDecoder().decode(photoUrl.substring(photoUrl.indexOf(",") + 1));

        response.setContentType("image/png");
        response.setHeader("Content-Disposition", "attachment; filename=\"passport-photo.png\"");

        OutputStream outputStream = response.getOutputStream();

        outputStream.write(bytes);
        outputStream.flush();
        outputStream.close();
    }

}