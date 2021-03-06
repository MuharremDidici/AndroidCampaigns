package com.companyname.campaigns;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.slider.library.Animations.DescriptionAnimation;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.daimajia.slider.library.Tricks.ViewPagerEx;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;

import java.util.HashMap;

public class ProductDetail extends AppCompatActivity implements BaseSliderView.OnSliderClickListener, ViewPagerEx.OnPageChangeListener {
    RatingBar ratingBar;



    static JSONObject proDt = null;
    private SliderLayout mDemoSlider;
    Button btnFavorites;
    WebView webView;
    TextView txtProDetailPrice ;
    DB db=new DB(this);
    Button btnAddCart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);
        webView = findViewById(R.id.webView);
        txtProDetailPrice = findViewById(R.id.txtProDetailPrice);
        webView.getSettings().setJavaScriptEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mDemoSlider = findViewById(R.id.slider);
        ratingBar =  findViewById(R.id.ratingBar);
        btnAddCart = findViewById(R.id.btnAddCart);
	btnFavorites=findViewById(R.id.btnFavorites);
	try {
           String rote= proDt.getJSONObject("likes").getJSONObject("like").getString("ortalama");
           ratingBar.setRating(Float.valueOf(rote));
            Log.d("likes ",rote);
        } catch (JSONException e) {
           Log.e("likes hatalı", String.valueOf(e));
        }
        try {

            String htmlData =
                            "<html>\n" +
                            "<head>\n" +
                            "<title>Home</title>\n" +
                            "</head>\n" +
                            "<body>\n" +
                                    "<p> "+proDt.getString("productName")+" </p>"+
                                    "<p> "+proDt.getString("description")+" </p>"+
                            "</body>\n" +
                            "</html>\n";

            getSupportActionBar().setTitle(proDt.getString("productName"));
            webView.setWebViewClient(new WebViewClient());
            webView.loadData(htmlData , "text/html; charset=UTF-8",null);
            txtProDetailPrice.setText(proDt.getString("price") +" TL");
        }catch (Exception e){
           
        }
        // image control
        HashMap<String,String> url_maps = new HashMap<String, String>();


        try {
            boolean imgControl = proDt.getBoolean("image");

            final int productId=proDt.getInt("productId");

           final String title = proDt.getString("productName");
           final String price=proDt.getString("price");
           final int fuserid=MainActivity.userInf.getUserId();

            if(imgControl) {
                JSONArray iArr = proDt.getJSONArray("images");
                for(int i = 0; i<iArr.length(); i++) {
                    String iur = iArr.getJSONObject(i).getString("normal");
                    url_maps.put(title+i, iur);
                }

                for(String name : url_maps.keySet()){
                    TextSliderView textSliderView = new TextSliderView(this);
                    // initialize a SliderLayout
                    textSliderView
                            .description(name)
                            .image(url_maps.get(name))
                            .setScaleType(BaseSliderView.ScaleType.FitCenterCrop)
                            .setOnSliderClickListener(this);

                    //add your extra information
                    textSliderView.bundle(new Bundle());
                    textSliderView.getBundle()
                            .putString("extra",name);

                    mDemoSlider.addSlider(textSliderView);
                }
                mDemoSlider.setPresetTransformer(SliderLayout.Transformer.Default);
                mDemoSlider.setPresetIndicator(SliderLayout.PresetIndicators.Center_Bottom);
                mDemoSlider.setCustomAnimation(new DescriptionAnimation());
                mDemoSlider.setDuration(4000);
                mDemoSlider.addOnPageChangeListener(this);





                btnFavorites.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        SQLiteDatabase ara=db.getWritableDatabase();
                        Cursor c=ara.rawQuery("select * from liste where fuserid='"+fuserid+"' and fproductid='"+productId+"'  " , null);

                          if (!c.moveToNext()) {
                            SQLiteDatabase yaz=db.getWritableDatabase();
                            //insert operation
                            ContentValues con=new ContentValues();
                            con.put("fuserid",fuserid);
                            con.put("fproductid",productId);
                            con.put("fProductTitle",title);
                            con.put("fProductMoney",price);

                            long yazSonuc=yaz.insert("liste",null,con);
                            if(yazSonuc>0){
                                Toast.makeText(ProductDetail.this, "Favorilerinize eklendi", Toast.LENGTH_SHORT).show();
                            }else{
                                Toast.makeText(ProductDetail.this, "Yazma Hatası !", Toast.LENGTH_SHORT).show();
                            }
                            yaz.close();
                            }
                        else{
                            Toast.makeText(ProductDetail.this, "Bu ürün favorilerinizde kayıtlıdır!", Toast.LENGTH_SHORT).show();
                        }




                    }
                });





            }else {
                // gösterilecek resim yok
            }
        }catch (Exception ex) {

        }
        String url="http://jsonbulut.com/json/likeManagement.php";
        raiting(ProductDetail.this,ratingBar,5,606,url);
        btnAddCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openDialog();
            }
        });
    }

    @Override
    protected void onStop() {
        // To prevent a memory leak on rotation, make sure to call stopAutoCycle() on the slider before activity or fragment is destroyed
        mDemoSlider.stopAutoCycle();
        super.onStop();
    }

    @Override
    public void onSliderClick(BaseSliderView slider) {
        Toast.makeText(this,slider.getBundle().get("extra") + "",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        Log.d("Slider Demo", "Page Changed: " + position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    private void openDialog() {
        CustomDialog dialog=new CustomDialog();
        dialog.show(getSupportFragmentManager(),"Cart Dialog");
    }

    // json raiting  data export edit:Muharrem
    class jsonDataRaiting extends AsyncTask<Void, Void, Void> {

        String url = "";
        HashMap<String,String> hm = new HashMap<>();
        Context cnx = null;
        String jsonString = "";
        // ProgressDialog pro;
        public jsonDataRaiting(Context cnx,String url,HashMap<String,String> hm){

            this.url = url;
            this.hm = hm;
            this.cnx=cnx;
            //pro = new ProgressDialog(cnx);
            // pro.setMessage("Yükleniyor Lütfen Bekleyiniz..");
            // pro.show();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                jsonString = Jsoup.connect(url).data(hm).timeout(30000).ignoreContentType(true).get().body().text();
                //  pro.hide();
            }catch (Exception ex) {
                // pro.hide();
                Toast.makeText(cnx, "İşlem Başarısız Oldu", Toast.LENGTH_SHORT).show();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {

            // grafiksel işlemler bu gövdede yer alır.
            if (!jsonString.equals("")){
                try {
                    JSONObject jobj = new JSONObject(jsonString);
                    boolean durum = jobj.getJSONArray("votes").getJSONObject(0).getBoolean("durum");
                    String mesaj = jobj.getJSONArray("votes").getJSONObject(0).getString("mesaj");
                    if(durum) {


                    }else {
                        Toast.makeText(cnx, mesaj, Toast.LENGTH_SHORT).show();
                    }
                }catch (Exception ex) {
                    Toast.makeText(cnx, "Json Pars Hatası", Toast.LENGTH_SHORT).show();
                }
            }else {
                Toast.makeText(cnx, "Sunucu Hatası Oluştur.. ", Toast.LENGTH_SHORT).show();
            }
            super.onPostExecute(aVoid);
        }

    }
    public void raiting(final Context rcnx, RatingBar ratingBar, final int cid, final int pid, final String url){

        final HashMap<String,String> hm=new HashMap<>();
        hm.put("ref","ce7f46683b56cb84131405b848678c51");
        hm.put("productId", String.valueOf(pid));
        hm.put("customerId",String.valueOf(cid));

        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float v, boolean b) {
                Log.d("RATING ", "onRatingChanged: "+v);

                hm.put("vote",String.valueOf(v));
                new jsonDataRaiting(rcnx,url, hm ).execute();


            }
        });

    }
	
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
