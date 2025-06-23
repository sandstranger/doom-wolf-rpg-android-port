
#ifndef OPENNMTTOKENIZER_EXPORT_H
#define OPENNMTTOKENIZER_EXPORT_H

#ifdef OPENNMTTOKENIZER_STATIC_DEFINE
#  define OPENNMTTOKENIZER_EXPORT
#  define OPENNMTTOKENIZER_NO_EXPORT
#else
#  ifndef OPENNMTTOKENIZER_EXPORT
#    ifdef OpenNMTTokenizer_EXPORTS
        /* We are building this library */
#      define OPENNMTTOKENIZER_EXPORT __attribute__((visibility("default")))
#    else
        /* We are using this library */
#      define OPENNMTTOKENIZER_EXPORT __attribute__((visibility("default")))
#    endif
#  endif

#  ifndef OPENNMTTOKENIZER_NO_EXPORT
#    define OPENNMTTOKENIZER_NO_EXPORT __attribute__((visibility("hidden")))
#  endif
#endif

#ifndef OPENNMTTOKENIZER_DEPRECATED
#  define OPENNMTTOKENIZER_DEPRECATED __attribute__ ((__deprecated__))
#endif

#ifndef OPENNMTTOKENIZER_DEPRECATED_EXPORT
#  define OPENNMTTOKENIZER_DEPRECATED_EXPORT OPENNMTTOKENIZER_EXPORT OPENNMTTOKENIZER_DEPRECATED
#endif

#ifndef OPENNMTTOKENIZER_DEPRECATED_NO_EXPORT
#  define OPENNMTTOKENIZER_DEPRECATED_NO_EXPORT OPENNMTTOKENIZER_NO_EXPORT OPENNMTTOKENIZER_DEPRECATED
#endif

/* NOLINTNEXTLINE(readability-avoid-unconditional-preprocessor-if) */
#if 0 /* DEFINE_NO_DEPRECATED */
#  ifndef OPENNMTTOKENIZER_NO_DEPRECATED
#    define OPENNMTTOKENIZER_NO_DEPRECATED
#  endif
#endif

#endif /* OPENNMTTOKENIZER_EXPORT_H */
