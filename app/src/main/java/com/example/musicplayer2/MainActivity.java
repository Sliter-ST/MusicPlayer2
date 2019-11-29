package com.example.musicplayer2;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, View.OnClickListener, SeekBar.OnSeekBarChangeListener, MediaPlayer.OnCompletionListener, AdapterView.OnItemLongClickListener {
    private List<Music> musicList = new ArrayList<>();
    private MusicDatabaseHelper MdbHelper;
    ListView listView;

    protected Button mPreviousBtn;
    protected Button mPlayBtn;
    protected Button mNextBtn;
    protected Button mRefresh;
    protected Button mSuiji;
    protected Button mShunxu;
    protected TextView mCurrentTimeTv;
    protected TextView mTotalTimeTv;
    protected SeekBar mSeekBar;


    private MediaPlayer mMediaPlayer;

    private int mCurrentPosition;   //歌曲位置
    private int nCurrentPosition;
    private int playMode = 0;

//    private static final int REQUEST_EXTERNAL_STORAGE = 1;
//
//    private static String[] PERMISSIONS_STORAGE = {
//            "android.permission.READ_EXTERNAL_STORAGE",
//            "android.permission.WRITE_EXTERNAL_STORAGE" };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        musicList = getPlaying();

        initView();

        MusicAdapter adapter = new MusicAdapter(MainActivity.this,R.layout.music_item,musicList);
        listView = (ListView)findViewById(R.id.list_view);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
        listView.setOnItemLongClickListener(this);






    }
//    //先定义
//    //然后通过一个函数来申请
//    public static void verifyStoragePermissions(Activity activity) {
//        try {
//            //检测是否有写的权限
//            int permission = ActivityCompat.checkSelfPermission(activity,
//                    "android.permission.WRITE_EXTERNAL_STORAGE");
//            if (permission != PackageManager.PERMISSION_GRANTED) {
//                // 没有写的权限，去申请写的权限，会弹出对话框
//                ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE,REQUEST_EXTERNAL_STORAGE);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    private void initView(){
        mPreviousBtn = (Button) findViewById(R.id.previous_btn);
        mPreviousBtn.setOnClickListener(MainActivity.this);
        mPlayBtn = (Button) findViewById(R.id.play_btn);
        mPlayBtn.setOnClickListener(MainActivity.this);
        mNextBtn = (Button) findViewById(R.id.next_btn);
        mNextBtn.setOnClickListener(MainActivity.this);
        mRefresh = (Button) findViewById(R.id.refresh);
        mRefresh.setOnClickListener(MainActivity.this);
        mSuiji = (Button) findViewById(R.id.suiji);
        mSuiji.setOnClickListener(MainActivity.this);
        mShunxu = (Button) findViewById(R.id.shunxu);
        mShunxu.setOnClickListener(MainActivity.this);

        mCurrentTimeTv = (TextView) findViewById(R.id.current_time_tv);
        mTotalTimeTv = (TextView) findViewById(R.id.total_time_tv);
        mSeekBar = (SeekBar) findViewById(R.id.seek_bar);


        // SeekBar绑定监听器，监听拖动到指定位置
        mSeekBar.setOnSeekBarChangeListener(this);


    }

    public boolean onCreateOptionsMenu(Menu menu){      //创建菜单
        //获取MenuInflater
        MenuInflater inflater = getMenuInflater();
        //加载Menu资源
        inflater.inflate(R.menu.menu,menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item){        //监听器
        switch (item.getItemId()) {
            case R.id.add:
                Intent intent = new Intent(MainActivity.this,LocalActivity.class);
                startActivity(intent);
                return true;
            case R.id.help:
                Help_Dialog();
                //Toast.makeText(this,"成功",Toast.LENGTH_SHORT).show();
                break;
            default:
        }
        return super.onOptionsItemSelected(item);
    }

    public void Help_Dialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.help_dialog,null)).setTitle("帮助");
        builder.setNegativeButton("OK",new DialogInterface.OnClickListener(){
            @Override
            public void  onClick(DialogInterface dialog,int id){
            }
        });
        builder.show();
    }


    //获取播放列表
    private List<Music> getPlaying(){
        List<Music> list = new ArrayList<>();
        MdbHelper = new MusicDatabaseHelper(this,"Music.db",null,1);
        SQLiteDatabase db = MdbHelper.getWritableDatabase();

        // 使用内容解析者访问系统提供的数据库
        Cursor cursor = db.query("music", null, null, null, null,null,null);// 默认排序顺序
        // 如果游标读取时还有下一个数据，读取
        if(cursor.moveToFirst()){
            do {
                String title = cursor.getString(cursor.getColumnIndex("title"));
                String artist = cursor.getString(cursor.getColumnIndex("artist"));
                String uri = cursor.getString(cursor.getColumnIndex("uri"));
                Music musics = new Music();
                musics.setTitle(title);
                musics.setArtist(artist);
                musics.setUri(uri);
                list.add(musics);
            }while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }


    //点击列表
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        mCurrentPosition = position;
        changeMusic(mCurrentPosition);
    }
/**
 *
 */

    //长按列表
    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        mCurrentPosition = position;
        final Music music = musicList.get(mCurrentPosition);
        PopupMenu popupMenu = new PopupMenu(this,view);
        popupMenu.getMenuInflater().inflate(R.menu.pop_menu,popupMenu.getMenu());
        popupMenu.show();

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.delete:
                        String sql = "delete from music where title='"+music.getTitle()+"'";
                        SQLiteDatabase db = MdbHelper.getReadableDatabase();
                        db.execSQL(sql);
                        break;
                    default:
                        break;
                }
                return false;
            }
        });
        return true;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        // 当歌曲播放完毕，切歌到下一首
        changeMusic(++mCurrentPosition);
    }

    //点击按钮动作
    @Override
    public void onClick(View view) {
            if(playMode == 0){
                if (view.getId() == R.id.previous_btn) {// 上一首
                            changeMusic(--mCurrentPosition);
                        } else if (view.getId() == R.id.play_btn) {// 播放/暂停

                            // 首次点击播放按钮，默认播放第0首
                            if (mMediaPlayer == null) {
                                changeMusic(0);
                            } else {
                                playOrPause();
                            }
                        } else if (view.getId() == R.id.next_btn) {// 下一首
                            changeMusic(++mCurrentPosition);
                        }
            }
            else{
                nCurrentPosition = (int) (Math.random() * musicList.size());
                        if (view.getId() == R.id.previous_btn) {// 上一首
                            changeMusic((int)nCurrentPosition);
                        } else if (view.getId() == R.id.play_btn) {// 播放/暂停

                            // 首次点击播放按钮，默认播放第0首
                            if (mMediaPlayer == null) {
                                changeMusic((int) nCurrentPosition);
                            } else {
                                playOrPause();
                            }
                        } else if (view.getId() == R.id.next_btn) {// 下一首
                            changeMusic((int) nCurrentPosition);
                        }
            }

        if (view.getId() == R.id.refresh){//刷新
            musicList = getPlaying();
            MusicAdapter adapter2 = new MusicAdapter(MainActivity.this,R.layout.music_item,musicList);
            listView = (ListView)findViewById(R.id.list_view);
            listView.setAdapter(adapter2);
            Toast.makeText(this,"刷新成功",Toast.LENGTH_SHORT).show();

        }else if (view.getId() == R.id.suiji){
            playMode = 1;
            Toast.makeText(this,"随机播放",Toast.LENGTH_SHORT).show();
        }else if (view.getId() == R.id.shunxu){
            playMode = 0;
            Toast.makeText(this,"顺序播放",Toast.LENGTH_SHORT).show();
        }

    }


    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    // 当手停止拖拽进度条时执行该方法
    // 获取拖拽进度
    // 将进度对应设置给MediaPlayer
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        int progress = seekBar.getProgress();
        mMediaPlayer.seekTo(progress);
    }

    // 播放或暂停
    private void playOrPause() {
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
        } else {
            mMediaPlayer.start();
        }
    }

    // 切歌
    private void changeMusic(int position) {


            if (position < 0) {
            mCurrentPosition = position = musicList.size() - 1;
        } else if (position > musicList.size() - 1) {
            mCurrentPosition = position = 0;
        }

        if (mMediaPlayer == null) {
            mMediaPlayer = new MediaPlayer();
            // 绑定播放完毕监听器
            mMediaPlayer.setOnCompletionListener(this);
        }

        try {

            // 切歌之前先重置，释放掉之前的资源
            mMediaPlayer.reset();
            // 设置播放源
            mMediaPlayer.setDataSource(musicList.get(position).getUri());
            // 开始播放前的准备工作，加载多媒体资源，获取相关信息
            mMediaPlayer.prepare();
            // 开始播放
            mMediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "播放错误", Toast.LENGTH_SHORT).show();
        }

        // 切歌时重置进度条并展示歌曲时长
        mSeekBar.setProgress(0);
        mSeekBar.setMax(mMediaPlayer.getDuration());
        mTotalTimeTv.setText(parseTime(mMediaPlayer.getDuration()));

        updateProgress();
    }

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {

            // 展示给进度条和当前时间
            int progress = mMediaPlayer.getCurrentPosition();
            mSeekBar.setProgress(progress);
            mCurrentTimeTv.setText(parseTime(progress));

            // 继续定时发送数据
            updateProgress();

            return true;
        }
    });

    // 每间隔1s通知更新进度
    private void updateProgress() {
        // 使用Handler每间隔1s发送一次空消息，通知进度条更新
        Message msg = Message.obtain();// 获取一个现成的消息
        mHandler.sendMessageDelayed(msg, INTERNAL_TIME);
    }

    // 解析时间
    private String parseTime(int oldTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("mm:ss");// 时间格式
        String newTime = sdf.format(new Date(oldTime));
        return newTime;
    }

    private static final int INTERNAL_TIME = 1000;// 音乐进度间隔时间


}
