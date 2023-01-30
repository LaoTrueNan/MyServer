#include <Queue.h>
void yangvi(int n){
    Queue<int> que;
    int s=0,k=0,i=1,j,t;
    int temp;
    que.enqueue(i);
    que.enqueue(i);
    for(i=1;i<=n;i++){
        
        que.enqueue(k);
        for(j=1;j<=i+2;j++){
            que.dequeue(t);
            temp = s+t;
            que.enqueue(temp);
            s=t;
            if(j!=i+2){cout<<s<<' ';}
        }
        cout<<endl;
    }
}

int main(){
    int n;
    cin>>n;
    yangvi(n);
}