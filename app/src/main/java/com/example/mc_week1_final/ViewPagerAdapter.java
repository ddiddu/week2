package com.example.mc_week1_final;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

public class ViewPagerAdapter extends PagerAdapter implements Filterable {

    private Context context;
    private LayoutInflater layoutInflater;

    private ArrayList<ImageItem> ImageDataList;
    private ArrayList<ImageItem> filteredList;

    public ViewPagerAdapter(Context context, ArrayList<ImageItem> list) {
        this.context=context;
        this.ImageDataList=list;
        this.filteredList=list;
    }

    public int getCount() {
        return filteredList.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
         return view==o;
     }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        layoutInflater=(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view=layoutInflater.inflate(R.layout.custom_layout,null);

        SubsamplingScaleImageView imageView=(SubsamplingScaleImageView)view.findViewById(R.id.myImageView);

        ImageItem item=filteredList.get(position);
        //idcolumn로부터 사진 불러오기
        Bitmap image = getImage(context, Integer.parseInt((item.getIdColum())));
        if(image != null) {
            imageView.setImage(ImageSource.bitmap(image));
        }
        else {    // 이미지 없을 경우
            imageView.setImage(ImageSource.resource(R.drawable.no_album_img));
        }

        ViewPager viewPager=(ViewPager)container;
        viewPager.addView(view);
        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        ViewPager viewPager=(ViewPager)container;
        View view=(View)object;
        viewPager.removeView(view);
    }

    // idColumn로 이미지 불러오기
    public static final BitmapFactory.Options options = new BitmapFactory.Options();

    public static Bitmap getImage(Context context, int idColumn) {

        ContentResolver res = context.getContentResolver();
        Uri uri = Uri.parse("content://media/external/images/media/" + idColumn);
        if (uri != null) {
            ParcelFileDescriptor fd = null;
            try {
                fd = res.openFileDescriptor(uri, "r");

                //크기를 얻어오기 위한옵션 ,
                //inJustDecodeBounds값이 true로 설정되면 decoder가 bitmap object에 대해 메모리를 할당하지 않고, 따라서 bitmap을 반환하지도 않는다.
                // 다만 options fields는 값이 채워지기 때문에 Load 하려는 이미지의 크기를 포함한 정보들을 얻어올 수 있다.

                options.inJustDecodeBounds = true;
                BitmapFactory.decodeFileDescriptor(
                        fd.getFileDescriptor(), null, options);
                int scale = 0;
                options.inJustDecodeBounds = false;
                options.inSampleSize = scale;

                Bitmap b = BitmapFactory.decodeFileDescriptor(
                        fd.getFileDescriptor(), null, options);

                if (b != null) {
                    // finally rescale to exactly the size we need
                }
                return b;
            } catch (FileNotFoundException e) {
            } finally {
                try {
                    if (fd != null)
                        fd.close();
                } catch (IOException e) {
                }
            }
        }
        return null;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {       // performFiltering, publishResults 필수
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String charString = constraint.toString();      // 입력받은 값 string으로 변경
                if (charString.isEmpty()) {
                    filteredList = ImageDataList;
                }             // 검색값 없으면, 전체 연락처
                else {
                    ArrayList<ImageItem> filteringList = new ArrayList<>();   // 필터링 중, 검색된 연락처 저장할 변수
                    for (ImageItem name : ImageDataList) {                    // 반복문으로 전체 필터 체크
                        if (name.getDisplayName().toLowerCase().contains(charString.toLowerCase())) {
                            filteringList.add(name);
                        }
                    }
                    filteredList = filteringList;       // 검색된 리스트ㄱ
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = filteredList;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                filteredList = (ArrayList<ImageItem>) results.values;
                notifyDataSetChanged();
            }
        };
    }
}
