package io.virtualapp.home;


import java.util.List;

import io.virtualapp.abs.BasePresenter;
import io.virtualapp.abs.BaseView;
import io.virtualapp.home.models.AppData;
import io.virtualapp.home.models.AppInfoLite;

/**
 * @author Lody
 *
 * view层和precenter层的抽象层，放在一个类里统一管理
 * 默认的修饰符指定该类只能在home包内被访问
 */
/* package */ class HomeContract {

	/* package */ interface HomeView extends BaseView<HomePresenter> {

        void showBottomAction();

        void hideBottomAction();

		void showLoading();

		void hideLoading();

		void loadFinish(List<AppData> appModels);

		void loadError(Throwable err);

		void showGuide();

		void addAppToLauncher(AppData model);

        void removeAppToLauncher(AppData model);

		void refreshLauncherItem(AppData model);

		void askInstallGms();
	}

	/* package */ interface HomePresenter extends BasePresenter {

		void launchApp(AppData data);

		void dataChanged();

		void addApp(AppInfoLite info);

		void deleteApp(AppData data);

        void createShortcut(AppData data);
    }

}
