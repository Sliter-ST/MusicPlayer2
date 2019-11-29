package com.example.musicplayer2;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class LocalActivity extends AppCompatActivity {
    private List<Music> LocalMusicList = new ArrayList<>();
    private MusicDatabaseHelper MdbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local);
        //FindMusic findMusic = new FindMusic();
        MdbHelper = new MusicDatabaseHelper(this,"Music.db",null,1);
/**
 *
 * */
        //传入本地数据
        //LocalMusicList = findMusic.getmusics(getContentResolver());

        if(ContextCompat.checkSelfPermission(LocalActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(LocalActivity.this,new String[]
                    {
                            Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
        }
        else
        {
            findMusic();
        }

        MusicAdapter adapter = new MusicAdapter(LocalActivity.this,R.layout.music_item,LocalMusicList);
        ListView listView = (ListView)findViewById(R.id.local_list_view);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Music music = LocalMusicList.get(position); //获取位置，获取信息，加入数据库
                String title = music.getTitle();
                String artist = music.getArtist();
                String uri = music.getUri();
                Log.d("name",title+"");
                SQLiteDatabase db = MdbHelper.getWritableDatabase();
                String sql = "insert into music(title,artist,uri)values(?,?,?)";
                db.execSQL(sql,new String[]{title,artist,uri});
                Toast.makeText(LocalActivity.this,music.getTitle()+"已加入歌单",Toast.LENGTH_SHORT).show();
            }
        });



    }

    public boolean onCreateOptionsMenu(Menu menu){      //创建菜单
        //获取MenuInflater
        MenuInflater inflater = getMenuInflater();
        //加载Menu资源
        inflater.inflate(R.menu.back,menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item){        //监听器
        switch (item.getItemId()) {
            case R.id.back:
                Intent intent = new Intent(LocalActivity.this,MainActivity.class);
                startActivity(intent);
                break;
            default:
        }
        return super.onOptionsItemSelected(item);
    }

    public void onRequestPermissionsResult(int requestCode,String[] permissions,int[] grantResults)
    {
        switch (requestCode)
        {
            case 1:
                if(grantResults.length>0&&grantResults[0]==PackageManager.PERMISSION_GRANTED)
                {
                    findMusic();
                }
                else
                {
                    Toast.makeText(this,"拒绝权限无法使用该程序！",Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
        }
    }

    public void findMusic() {
        Cursor cursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        //List<Music> musics = new ArrayList<Music>();
        for (int i = 0; i < cursor.getCount(); i++) {
            Music music = new Music();     //新建一个歌曲对象,将从cursor里读出的信息存放进去,直到取完cursor里面的内容为止.
            cursor.moveToNext();
            long id = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
            String title = cursor.getString((cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)));//音乐标题
            String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));//艺术家
            long duration = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));//时长
            String uri = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));	//文件路径
            String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)); //唱片图片
            long album_id = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)); //唱片图片ID
            int isMusic = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.IS_MUSIC));//是否为音乐
            if (isMusic != 0 && duration/(1000 * 60) >= 1) {		//只把1分钟以上的音乐添加到集合当中
                music.setId(id);
                music.setTitle(title);
                music.setArtist(artist);
                music.setDuration(duration);
                music.setUri(uri);
                //  music.setAlbum_id(album_id);
                LocalMusicList.add(music);
            }
        }
        cursor.close();
        //return LocalMusicList;
    }
}
