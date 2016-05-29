-repackageclasses ''
-allowaccessmodification

-optimizations class/*,
               field/*,
               # Disabled, incompatible with com.google.android.gms.measurement
               # method/marking/final,
               method/marking/private,
               method/marking/static,
               method/removal/parameter,
               method/propagation/returnvalue,
               method/inlining/*,
               code/simplification/*,
               code/removal

