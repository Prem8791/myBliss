# Include from the target product makefile after the base product inherits common packages.
# Example:
#   $(call inherit-product, packages/apps/HomeLauncher/rom-integration/product/home_launcher_product.mk)

PRODUCT_PACKAGES += \
    HomeLauncher \
    privapp-permissions-com.home.launcher

# Mandatory only after HomeLauncher implements android.intent.action.QUICKSTEP_SERVICE:
# PRODUCT_PACKAGES += HomeLauncherConfigOverlay

# Optional final-ROM cleanup after HomeLauncher replaces Launcher3/QuickStep:
# PRODUCT_PACKAGES := $(filter-out Launcher3QuickStep,$(PRODUCT_PACKAGES))
