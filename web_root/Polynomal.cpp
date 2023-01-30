#include <Polynomal.h>
int main(){
    Polynomal p;
    cin>>p;
    Polynomal p2;
    cin>>p2;
    Polynomal p3;
    p3 = p * p2;
    cout<<p<<endl;
    cout<<p2<<endl;
    cout<<p3<<endl;
    return 0;
    // 取指针下的成员变量用箭头(->)，取到某个变量之后就可以用点（.）了
    // cout<t.link<<endl;
}