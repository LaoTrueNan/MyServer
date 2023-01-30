#include <iostream>
#include <vector>
using namespace std;

int main(){
    int l;
    cin>>l;
    if(l==0){
        cout<<0;
        return 0;
    }
    if(l<0){
        cout<<'-';
    }
    int temp  = l;
    int ab = temp%10;
    while(ab==0){
        temp/=10;
        ab=temp%10;
    }
    while(temp!=0){
        if(temp%10<0){
            cout<<0-temp%10;
        }else{
            cout<<temp%10;
        }
        temp/=10;
    }

}