namespace java t
namespace py thriftp

struct W {
  1: string a;
  2: optional i64 c;
  3: optional double v;
  4: i64 t;
}

service BankService {

   i64 c(1:W w),
   bool d(1:W w),
   bool w(1:W w),
   double b(1:W w),

}
