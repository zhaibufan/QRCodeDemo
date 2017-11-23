package com.example.zhai.qrcodedemo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.DecodeHintType;
import com.google.zxing.EncodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import com.google.zxing.qrcode.QRCodeWriter;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private EditText mEtContent;
    private ImageView mIvCode;
    private TextView mTvResult;
    private int[] pixels;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mEtContent = (EditText) findViewById(R.id.edit_content);
        mIvCode = (ImageView) findViewById(R.id.iv_code);
        mTvResult = (TextView) findViewById(R.id.tv_result);
    }

    public void generate(View view) {
        String content = mEtContent.getText().toString();
        Bitmap bitmap = generateBitmap(content, 200, 200);
        Bitmap b = BitmapFactory.decodeResource(this.getResources(), R.mipmap.ic_launcher);
        Bitmap result = addLogo(bitmap, b);
        mIvCode.setImageBitmap(result);
        scanningImage();
    }

    /**
     * 生成二维码
     *
     * @param content
     * @param width
     * @param height
     * @return
     */
    private Bitmap generateBitmap(String content, int width, int height) {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        Map<EncodeHintType, String> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
        try {
            BitMatrix encode = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, width, height, hints);
            pixels = new int[width * height];
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    if (encode.get(j, i)) {
                        pixels[i * width + j] = 0x00000000;
                    } else {
                        pixels[i * width + j] = 0xffffffff;
                    }
                }
            }
            return Bitmap.createBitmap(pixels, 0, width, width, height, Bitmap.Config.RGB_565);
        } catch (WriterException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 给二维码中心添加自己的logo
     *
     * @param qrBitmap
     * @param logoBitmap
     * @return
     */
    private Bitmap addLogo(Bitmap qrBitmap, Bitmap logoBitmap) {
        int qrBitmapWidth = qrBitmap.getWidth();
        int qrBitmapHeight = qrBitmap.getHeight();
        int logoBitmapWidth = logoBitmap.getWidth();
        int logoBitmapHeight = logoBitmap.getHeight();
        // 创建一个和原二维码图片一样大的空白bitmap
        Bitmap blankBitmap = Bitmap.createBitmap(qrBitmapWidth, qrBitmapHeight, Bitmap.Config.ARGB_8888);
        // 创建画布，传入blankBitmap，这样绘制的东西都会在blankBitmap上面
        Canvas canvas = new Canvas(blankBitmap);
        // 绘制二维码的图片
        canvas.drawBitmap(qrBitmap, 0, 0, null);
        // 将当前的绘制状态保存下来
        canvas.save(Canvas.ALL_SAVE_FLAG);

        // 对画布进行缩放，一帮情况下logo的宽高为二维码原图宽高的1/5
        float scaleSize = 1.0f;
        while ((logoBitmapWidth / scaleSize) > (qrBitmapWidth / 5) || (logoBitmapHeight / scaleSize) > (qrBitmapHeight / 5)) {
            scaleSize *= 2;
        }
        float sx = 1.0f / scaleSize;
        Log.d(TAG, "scaleSize="+scaleSize);
        canvas.scale(sx, sx, qrBitmapWidth / 2, qrBitmapHeight / 2);

        // 绘制logo的图片，logo必须在二维码的中心
        canvas.drawBitmap(logoBitmap, (qrBitmapWidth - logoBitmapWidth) / 2, (qrBitmapHeight - logoBitmapHeight) / 2, null);
        canvas.restore();
        return blankBitmap;
    }

    /**
     * 解析二维码
     */
    private void scanningImage() {
        Hashtable<DecodeHintType, String> hints = new
                Hashtable<DecodeHintType, String>();
        hints.put(DecodeHintType.CHARACTER_SET, "utf-8");

        // 获得待解图片
        Bitmap bitmap = ((BitmapDrawable) mIvCode.getDrawable()).getBitmap();
        RGBLuminanceSource source = new RGBLuminanceSource(bitmap.getWidth(), bitmap.getHeight(), pixels);
        BinaryBitmap bitmap1 = new BinaryBitmap(new HybridBinarizer(source));
        QRCodeReader reader = new QRCodeReader();
        Result result;
        try {
            result = reader.decode(bitmap1);
            result=reader.decode(bitmap1, hints );
            // 得到解析后的文字
            mTvResult.setText(result.getText());
        } catch (NotFoundException e) {
            e.printStackTrace();
        } catch (ChecksumException e) {
            e.printStackTrace();
        } catch (FormatException e) {
            e.printStackTrace();
        }
    }
}
