#pragma version(1)
#pragma rs java_package_name(com.android.camera.effects.RSFilter)

uchar* input;
uchar* output;	

uchar4* bmpInput;
uchar* yuvOutput;

int mImageWidth;
int mImageHeight;

int rotateDegree;

static uchar4 yuv_to_argb(uchar y, char u, char v){
	uchar4 out;
	
	int r = y + (1.772f*v);
    int g = y - (0.344f*v + 0.714f*u);
    int b = y + (1.402f*u);
    
    out.r = r>255? 255 : r<0 ? 0 : r;
    out.g = g>255? 255 : g<0 ? 0 : g;
    out.b = b>255? 255 : b<0 ? 0 : b;
    out.a = 0xff;
    
    return out;
}

/*

Y = ( (  66 * R + 129 * G +  25 * B + 128) >> 8) +  16
U = ( ( -38 * R -  74 * G + 112 * B + 128) >> 8) + 128
V = ( ( 112 * R -  94 * G -  18 * B + 128) >> 8) + 128
*/

static uchar3 argb_to_yuv(uchar4 argb){
	uchar3 out;
	int R=argb.r;
	int G=argb.g;
	int B=argb.b;
	int y=( (  66 * R + 129 * G +  25 * B + 128) >> 8) +  16;
	int u=( ( -38 * R -  74 * G + 112 * B + 128) >> 8) + 128;
	int v=( ( 112 * R -  94 * G -  18 * B + 128) >> 8) + 128;
	
	
	out.x = y>255? 255 : y<0 ? 0 : y;
    out.y = u>255? 255 : u<0 ? 0 : u;
    out.z = v>255? 255 : v<0 ? 0 : v;
	
	return out;
}

void __attribute__((kernel)) process_jpg2yuv(int sY){

	for(int i=0;i<mImageWidth;i++){
		int sX=i;
		int location=sY*mImageWidth+sX;
		uchar4 argb;
		argb.r=bmpInput[location].r;
		argb.g=bmpInput[location].g;
		argb.b=bmpInput[location].b;
		argb.a=bmpInput[location].a;
		
		
		uchar3 yuv=argb_to_yuv(argb);
		int offset=mImageWidth*mImageHeight;		
		int vIndex=(sY/2)*mImageWidth+(sX/2)*2+offset;
		int uIndex=vIndex+1;
		yuvOutput[location]=yuv.x;//y
		/*
		if(yuvOutput[vIndex]==0){
			yuvOutput[vIndex]=yuv.z;//v
			yuvOutput[uIndex]=yuv.y;//u
		}*/
		
		if(sY%2==0&&sX%2==0)
		{
			yuvOutput[uIndex]=yuv.y;
		}else if(sY%2==0&&sX%2==1)
		{
			yuvOutput[vIndex]=yuv.z;
		}
		
		/*
		yuvOutput[location*4]=argb.r;
		yuvOutput[location*4+1]=argb.g;
		yuvOutput[location*4+2]=argb.b;
		yuvOutput[location*4+3]=argb.a;
		*/
		
	}
	
}

void __attribute__((kernel)) process(int sY){

	for(int i=0;i<mImageWidth;i++){
		int sX=i;
		
		int yIndex=sY*mImageWidth+sX;
		
		int bmpLocation=yIndex;
		if(rotateDegree==90){
			bmpLocation=(sX+1)*mImageHeight-sY-1;
		}else if(rotateDegree==270){
			bmpLocation=(mImageWidth-sX-1)*mImageHeight+mImageHeight-sY-1;
		}
		
		
		int uvOffset=mImageWidth*mImageHeight;
		
		int vIndex=uvOffset+(sY/2)*mImageWidth+(sX/2)*2;
		int uIndex=vIndex+1;
		
		uchar4 rgba=yuv_to_argb(input[yIndex],input[uIndex]-128,input[vIndex]-128);
		
		output[bmpLocation*4]=rgba.r;
		output[bmpLocation*4+1]=rgba.g;
		output[bmpLocation*4+2]=rgba.b;
		output[bmpLocation*4+3]=rgba.a;
		
		
		//output[yIndex]=right_move(rgba.a,24)|right_move(rgba.r,16)|right_move(rgba.g,8)|right_move(rgba.b,0);
		
	}			
}