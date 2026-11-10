Slay the puzzle  -  لعبة أندرويد (بازل الكرة الخشبية)
برمجة وتطوير: محمد العمري  730215825

طريقة التشغيل:
1) افتح المجلد SlayThePuzzle في Android Studio (File > Open) ثم اضغط Run.
   أو من الطرفية:  gradle assembleDebug  (يتطلب Gradle 8.0+ و JDK 17)
2) المشروع بدون أي مكتبات خارجية (لا AndroidX) والحد الأدنى Android 5.0 (API 21).
3) إن كانت أداة البناء عندك قديمة (AGP أقدم من 7.3) احذف السطر namespace من app/build.gradle وأضف package="com.alomari.slaypuzzle" في AndroidManifest.xml.

طريقة اللعب:
- اسحب بإصبعك (أعلى/أسفل/يمين/يسار) لتنزلق الكرة حتى تصطدم بجدار.
- الكرة تطلي كل الخلايا التي تمر عليها باللون البرتقالي.
- أكمل طلاء كل الأرضية الخشبية لتفوز بالمستوى (1-3 نجوم حسب عدد الحركات).
- أزرار: رجوع، إعادة المستوى، تراجع عن آخر حركة.

الملفات الرئيسية (app/src/main/java/com/alomari/slaypuzzle):
 MenuActivity/MenuView     الواجهة الرئيسية التفاعلية
 LevelsActivity/LevelsView قائمة المستويات (120 مستوى)
 GameActivity/GameView     شاشة اللعب
 Board                     منطق اللعب + رسم اللوحة الخشبية
 LevelGen                  توليد مستويات قابلة للحل دائماً
 Sfx                       مؤثرات صوتية مولّدة برمجياً
 Ui / Particles / Progress أدوات الرسم والجسيمات وحفظ التقدم
