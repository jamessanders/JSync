#include <sys/types.h>
#include <sys/stat.h>
#include <stdlib.h>
#include <sys/time.h>
#include <utime.h>
#include <unistd.h>
#include <limits.h>

struct shortstat {
  mode_t st_mode;
  uid_t st_uid;
  gid_t st_gid;
  time_t statime;
  time_t stmtime; 
};

int shortstat(const char *path, struct shortstat *buf) {
  struct stat *s = malloc(sizeof(struct stat));
  int ret = stat(path, s);
  buf->st_mode = s->st_mode;
  buf->st_uid  = s->st_uid;
  buf->st_gid  = s->st_gid;
  buf->statime = s->st_atime;
  buf->stmtime = s->st_mtime;
  free(s);
  return ret;
}

