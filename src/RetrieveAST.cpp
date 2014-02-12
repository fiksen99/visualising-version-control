#include "gcc-plugin.h"
#include "plugin-version.h"

using namespace std; 

int plugin_is_GPL_compatible; //needed for gcc integration

struct plugin_info info = {.version="1", .help=NULL};

void f(void *gcc_data, void *user_data) {
	printf("made it!\n");
}

void f1(void *gcc_data, void *user_data) {
	printf("pass started!\n");
}

int __attribute__((visibility("default"))) plugin_init(
    struct plugin_name_args *plugin_info, struct plugin_gcc_version *version) {

  if(!plugin_default_version_check(version, &gcc_version))
  	return 1;

  register_callback(plugin_info->base_name, PLUGIN_PRE_GENERICIZE, &f, NULL);
  register_callback(plugin_info->base_name, PLUGIN_ALL_PASSES_START, &f1, NULL);

  return 0;
}