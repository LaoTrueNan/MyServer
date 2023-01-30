#include <iostream>
using namespace std;
int howManyTwo(int x) {
    // 统计[a,b]中数字2的个数
    int cnt = 0;
    for(int i =1;i<=x;i*=10){
        int a = x/(i*10),b=x/i%10,c=x%i;
        if(b>2) cnt+=(a+1)*i;
        else if(b==2) cnt+=a*i+c+1;
        else if(b<2) cnt+=a*i;
    }
    return cnt;
}

bool checkPrime(int target){
    if(target<=2){
        return false;
    }
    for(int i=2;i<=target-1;i++){
        if(target%i==0){
            return false;
        }
    }
    return true;
}

void goldBachConjecture(int x){
    // 哥德巴赫猜想 任意偶数N,4-N都可以写成两个质数之和,多种组合输出加数较小者 10=3+7=5+5,则输出10=3+7 
    if(x%2!=0){
        exit(-2);
    }else{
        for(int i = 2;i<=x/2;i++){
            if(checkPrime(i) && checkPrime(x-i)){
                cout<<x<<"="<<i<<"+"<<x-i<<endl;
                break;
            }
        }
    }
}

int main(){
    int n;
    cin>>n;
    for(int i=4;i<=n;i+=2){
        goldBachConjecture(i);
    }
    return 0;
}